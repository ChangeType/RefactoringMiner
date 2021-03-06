package gr.uom.java.xmi.decomposition;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;

import gr.uom.java.xmi.LocationInfo.CodeElementType;

public class TernaryOperatorExpression {

	private AbstractExpression condition;
	private AbstractExpression thenExpression;
	private AbstractExpression elseExpression;
	private String expression;

	public TernaryOperatorExpression(CompilationUnit cu, String filePath, ConditionalExpression expression) {
		this.condition = new AbstractExpression(cu, filePath, expression.getExpression(), CodeElementType.TERNARY_OPERATOR_CONDITION);
		this.thenExpression = new AbstractExpression(cu, filePath, expression.getThenExpression(), CodeElementType.TERNARY_OPERATOR_THEN_EXPRESSION);
		this.elseExpression = new AbstractExpression(cu, filePath, expression.getElseExpression(), CodeElementType.TERNARY_OPERATOR_ELSE_EXPRESSION);
		this.expression = expression.toString();
	}

	public AbstractExpression getCondition() {
		return condition;
	}

	public AbstractExpression getThenExpression() {
		return thenExpression;
	}

	public AbstractExpression getElseExpression() {
		return elseExpression;
	}

    public String getExpression() {
    	return expression;
    }
}
