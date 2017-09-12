package kalang.ast;
import java.util.*;
import kalang.core.*;
/**
 * The ElementExpr presents a element of a array,for example <code>args[i]</code>
 * 
 * @author Kason Yang 
 */
public class ElementExpr extends AssignableExpr{
    
    protected ExprNode arrayExpr;
    
    protected ExprNode index;
    
    public ElementExpr(ExprNode target,ExprNode key){
            this.arrayExpr = target;
            this.index = key;
    }
    
    @Override
    public List<AstNode> getChildren(){
        List<AstNode> ls = new LinkedList();
        addChild(ls, arrayExpr);
        addChild(ls, index);
        return ls;
    }

    @Override
    public Type getType() {
        Type arrayType = getType(getArrayExpr());
        if(arrayType==null) return Types.VOID_TYPE;
        if(!(arrayType instanceof ArrayType)){
            throw new UnknownError("ArrayType is required!");
        }
        return ((ArrayType)arrayType).getComponentType();
    }

    /**
     * @return the arrayExpr
     */
    public ExprNode getArrayExpr() {
        return arrayExpr;
    }

    /**
     * @param arrayExpr the arrayExpr to set
     */
    public void setArrayExpr(ExprNode arrayExpr) {
        Objects.requireNonNull(arrayExpr);
        this.arrayExpr = arrayExpr;
    }

    /**
     * @return the index
     */
    public ExprNode getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(ExprNode index) {
        Objects.requireNonNull(index);
        this.index = index;
    }
    
}