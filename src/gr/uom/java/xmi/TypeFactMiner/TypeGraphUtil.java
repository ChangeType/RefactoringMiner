package gr.uom.java.xmi.TypeFactMiner;


import com.t2r.common.models.ast.TypeGraphOuterClass.TypeGraph;
import com.t2r.common.models.ast.TypeGraphOuterClass.TypeGraph.Builder;
import com.t2r.common.models.ast.TypeNodeOuterClass.TypeNode;
import com.t2r.common.models.ast.TypeNodeOuterClass.TypeNode.TypeKind;
import gr.uom.java.xmi.TypeFactMiner.TypFct.Context;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.t2r.common.models.ast.TypeNodeOuterClass.TypeNode.TypeKind.Primitive;
import static java.util.stream.Collectors.*;

public class TypeGraphUtil {

    public static TypFct getTypeFact(Type t, Context c){
        return new TypFct(getTypeGraph(t),c);
    }

    public static TypFct getTypeGraphStripParam(Type t, CompilationUnit c){
        return new TypFct(getTypeGraphStripParam(t),new Context(c, Optional.empty()));
    }

    public static TypFct getTypeGraph(Type t, CompilationUnit c){
        return new TypFct(getTypeGraph(t),new Context(c, Optional.empty()));
    }

    public static TypFct getTypeFact(TypeGraph t, Context c){
        return new TypFct(t,c);
    }

    public static TypFct getTypeFactDontResolve(TypeGraph t, Context c){
        return new TypFct(t,c, true);
    }

    public static TypFct getTypeFact(TypeGraph t, Context c, GlobalContext gc){
        return new TypFct(t,c, gc);
    }

    public static Stream<Tuple2<TypFct, Long>> getTypeFact(List<TypeGraph> t, Context c, GlobalContext gc){
        Map<TypeGraph, Long> ts = t.stream().collect(groupingBy(x -> x, counting()));
        return ts.entrySet().stream().map(Tuple::fromEntry)
                .map(x -> x.map1(f -> new TypFct(f, c, gc)));


    }

    public static TypeGraph getTypeGraphStripParam(Type t) {
        if(t.isQualifiedType())
            return getTypeGraph((QualifiedType) t);
        else if(t.isNameQualifiedType())
            return getTypeGraph((NameQualifiedType) t);
        else if(t.isSimpleType())
            return getTypeGraph((SimpleType) t);
        else if(t.isParameterizedType())
            return getTypeGraphStripParam((ParameterizedType) t);
        else if(t.isWildcardType())
            return getTypeGraphStripParam( (WildcardType) t);
        else if(t.isPrimitiveType())
            return getTypeGraph((PrimitiveType) t);
        else if(t.isArrayType())
            return getTypeGraphStripParam((ArrayType) t);
//        else if(t.isIntersectionType())
//            return getTypeGraph((IntersectionType) t);
//        else if(t.isUnionType())
//            return getTypeGraph((UnionType) t);
        else
            throw new RuntimeException("Could not figure out type");

    }


    public static TypeGraph getTypeGraph(Type t) {
        if(t.isQualifiedType())
            return getTypeGraph((QualifiedType) t);
        else if(t.isNameQualifiedType())
            return getTypeGraph((NameQualifiedType) t);
        else if(t.isSimpleType())
            return getTypeGraph((SimpleType) t);
        else if(t.isParameterizedType())
            return getTypeGraph((ParameterizedType) t);
        else if(t.isWildcardType())
            return getTypeGraph( (WildcardType) t);
        else if(t.isPrimitiveType())
            return getTypeGraph((PrimitiveType) t);
        else if(t.isArrayType())
            return getTypeGraph((ArrayType) t);
        else if(t.isIntersectionType())
            return getTypeGraph((IntersectionType) t);
        else if(t.isUnionType())
            return getTypeGraph((UnionType) t);
        else
            throw new RuntimeException("Could not figure out type");

    }

    private static TypeNode getTypeNode(String name, List<String> annotations, TypeKind tk){
        return TypeNode.newBuilder().setKind(tk).setName(name).build();
    }

    private static TypeNode getTypeNode(TypeKind tk){
        return TypeNode.newBuilder().setKind(tk).build();
    }

    private static TypeNode getTypeNode(TypeKind tk, List<String> annotataions){
        return TypeNode.newBuilder().setKind(tk).build();
    }


    private static TypeGraph getTypeGraph(SimpleType st){
        final List<Annotation> ann = st.annotations();
        final List<String> annotation = ann.stream().map(a -> "@" + a.getTypeName().getFullyQualifiedName())
                    .collect(toList());
        return of(getTypeNode(st.getName().getFullyQualifiedName(), annotation, TypeKind.Simple));

    }

    private static TypeGraph getTypeGraph(ParameterizedType pt){
        final List<Type> ps = pt.typeArguments();
        final List<TypeGraph> params = ps.stream().map(TypeGraphUtil::getTypeGraph).collect(toList());
        final TypeNode root = getTypeNode(TypeKind.Parameterized);
        return of(root).toBuilder()
                .putEdges("of", getTypeGraph(pt.getType()))
                .putAllEdges(IntStream.range(0,params.size()).mapToObj(x->x)
                        .collect(toMap(x-> ("Param:"+x) , x-> params.get(x))))
                .build();
    }

    private static TypeGraph getTypeGraphStripParam(ParameterizedType pt){
        return getTypeGraph(pt.getType());
    }

    private static TypeGraph getTypeGraph(WildcardType wt) {
        final List<Annotation> ann = wt.annotations();
        final List<String> annotation = ann.stream().map(a -> "@" + a.getTypeName().getFullyQualifiedName())
                .collect(toList());
        final TypeNode root = getTypeNode(TypeKind.WildCard, annotation);
        Builder bldr = of(root).toBuilder();
        if(wt.getBound()!=null){
            bldr.putEdges(wt.isUpperBound() ? "extends" : "super", getTypeGraph(wt.getBound()));
        }
        return bldr.build();

    }

    private static TypeGraph getTypeGraphStripParam(WildcardType wt) {
        return getTypeGraph(wt.getBound());
    }


    private static TypeGraph getTypeGraph(PrimitiveType pt){
        final List<Annotation> ann = pt.annotations();
        final List<String> annotation = ann.stream().map(a -> "@" + a.getTypeName().getFullyQualifiedName())
                .collect(toList());
        return of(getTypeNode(pt.getPrimitiveTypeCode().toString(), annotation, Primitive));
    }

    private static TypeGraph getTypeGraph(ArrayType at){
        final List<Dimension> ds = at.dimensions();
        final List<String> annotation = new ArrayList<>();
        for(Dimension d : ds){
            List<Annotation> aa = d.annotations();
            for(Annotation a: aa){
                annotation.add("@" + a.getTypeName().getFullyQualifiedName());
            }
        }
        final TypeNode root = getTypeNode(TypeKind.Array, annotation);
        return of(root).toBuilder()
                .putEdges("of", getTypeGraph(at.getElementType())).build();
    }

    private static TypeGraph getTypeGraphStripParam(ArrayType at){
        return getTypeGraph(at.getElementType());
    }


    private static TypeGraph getTypeGraph(IntersectionType it){
        final List<Type> ts = it.types();
        final TypeNode root = getTypeNode(TypeKind.Intersection);
        return of(root).toBuilder()
                .putAllEdges(IntStream.range(0,ts.size()).mapToObj(x->x)
                        .collect(toMap(x-> ("Intersection:"+x) , x-> getTypeGraph(ts.get(x))))).build();
    }

    private static TypeGraph getTypeGraph(UnionType it){
        final List<Type> ts = it.types();
        final TypeNode root = getTypeNode(TypeKind.Union);
        return of(root).toBuilder()
                .putAllEdges(IntStream.range(0,ts.size()).mapToObj(x->x)
                        .collect(toMap(x-> ("Union:"+x) , x-> getTypeGraph(ts.get(x)))))
                .build();
    }

    private static TypeGraph getTypeGraph(QualifiedType q){
        final List<Annotation> ann = q.annotations();
        final List<String> annotation = ann.stream().map(a -> "@" + a.getTypeName().getFullyQualifiedName())
                .collect(toList());
        final String name = getTypeGraph(q.getQualifier()).getRoot().getName() +"." + q.getName().getIdentifier();
        return of(getTypeNode(name, annotation, TypeKind.Simple));
    }

    private static TypeGraph getTypeGraph(NameQualifiedType nq){
        final List<Annotation> ann = nq.annotations();
        final List<String> annotation = ann.stream().map(a -> "@" + a.getTypeName().getFullyQualifiedName())
                .collect(toList());
        final String name = nq.getQualifier().getFullyQualifiedName() + "." + nq.getName().getIdentifier();
        return of(getTypeNode(name, annotation, TypeKind.Simple));
    }

    private static TypeGraph of(TypeNode root){
        return TypeGraph.newBuilder().setRoot(root).build();
    }



//    public static String getTypeSemCategory(TypeGraph resolvedTypeGraph){
//        switch (resolvedTypeGraph.getRoot().getKind()){
//            case Primitive:
//            case Simple:
//                return resolvedTypeGraph.getRoot().getTypeSem().name();
//            case WildCard:{
//                if(resolvedTypeGraph.getEdgesMap().get("of")!=null){
//                    return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("of"));
//                }else{
//                    return TypeNode.TypeSem.Object.name();
//                }
//            }
//            case Array:
//            case Parameterized:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("of"));
//            case Intersection:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("Intersection:"));
//            case Union:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("Union:"));
//            default: return TypeNode.TypeSem.Object.name();
//        }
//
//    }
//
//
//    public static String getNameSpaceCategory(TypeGraph resolvedTypeGraph){
//        switch (resolvedTypeGraph.getRoot().getKind()){
//            case Primitive:
//            case Simple:
//                return resolvedTypeGraph.getRoot().getNamespacee().name();
//            case WildCard:{
//                if(resolvedTypeGraph.getEdgesMap().get("of")!=null){
//                    return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("of"));
//                }else{
//                    return TypeNode.NameSpace.Jdk.name();
//                }
//            }
//            case Array:
//            case Parameterized:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("of"));
//            case Intersection:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("Intersection:"));
//            case Union:
//                return getNameSpaceCategory(resolvedTypeGraph.getEdgesMap().get("Union:"));
//            default: return TypeNode.NameSpace.Jdk.name();
//        }

//    }


}
