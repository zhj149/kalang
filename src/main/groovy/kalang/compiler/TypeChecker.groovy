package kalang.compiler

import java.lang.reflect.Modifier;
import java.util.List;

import jast.ast.AbstractAstVisitor
import jast.ast.AssignExpr;
import jast.ast.AstNode
import jast.ast.AstVisitor
import jast.ast.BinaryExpr;
import jast.ast.CastExpr;
import jast.ast.CatchStmt;
import jast.ast.ClassExpr;
import jast.ast.ClassNode
import jast.ast.ConstExpr;
import jast.ast.ElementExpr;
import jast.ast.ExprNode
import jast.ast.FieldExpr;
import jast.ast.FieldNode
import jast.ast.IfStmt
import jast.ast.InvocationExpr;
import jast.ast.LoopStmt;
import jast.ast.MethodNode
import jast.ast.NewArrayExpr
import jast.ast.NewExpr;
import jast.ast.ParameterExpr;
import jast.ast.ParameterNode;
import jast.ast.ReturnStmt;
import jast.ast.Statement
import jast.ast.TryStmt;
import jast.ast.UnaryExpr;
import jast.ast.VarDeclStmt;
import jast.ast.VarExpr;
import kalang.compiler.AstError as CE
@groovy.transform.TypeChecked
class TypeChecker extends AstVisitor<String> {
    
    static class TypeError extends Exception{
        
        AstNode node
        
        int errorCode

        public TypeError(int errorCode,String msg,AstNode node){
            super(msg)
            this.node = node
            this.errorCode =errorCode;
        }
    }

    private static final String FLOAT_CLASS = "java.lang.Float";
    private static final String DOUBLE_CLASS = "java.lang.Double";

    private static final String INT_CLASS = "java.lang.Integer";
    private static final String LONG_CLASS = "java.lang.Long";

    private static final String BOOLEAN_CLASS = "java.lang.Boolean";

    private static final String CHAR_CLASS = "java.lang.Character";

    private static final String STRING_CLASS = "java.lang.String";

    private static final String NULL_CLASS = "java.lang.NullObject";

    private static final String DEFAULT_CLASS = "java.lang.Object";
    
	private static final String VOID_TYPE = "void"    
	
    private HashMap<Integer,VarDeclStmt> varDeclStmts = [:]
    
	HashMap<String,FieldNode> fields
	
    AstLoader astLoader

    ClassNode clazz
	
	TypeSystem castSys
	
	AstParser astParser
	
	MethodNode method
	
	boolean returned
	
	private Stack<List<String>> exceptionStack = new Stack()

    TypeChecker(AstLoader astLoader){
        this.astLoader = astLoader
		this.castSys = new TypeSystem(astLoader);
		this.astParser = new AstParser(astLoader);
    }
    
    private ExprNode cast(ExprNode expr,String from,String to,AstNode node){
        expr = castSys.cast(expr,from,to)
		if(!expr){
            CE.failedToCast(node,from,to);
            //fail(CompileError.UNABLE_TO_CAST,"${from} => ${to}",node)
        }
		return expr
    }
    
    public ClassNode loadAst(String name,AstNode node){
        ClassNode ast = this.astLoader.getAst(name);
        if(ast==null) 
        CE.classNotFound(node,name);
            //fail(CE.CLASS_NOT_FOUND,name,node)
        return ast;
    }

    public void check(ClassNode clz){
        this.fields = [:]
        for(def f in clz.fields){
            this.fields.put(f.name,f)
        }
        this.clazz = clz
        visit(clazz)
        if(clazz.interfaces?.size()>0){
            for(def itfName in clazz.interfaces){
                def itfNode = this.loadAst(itfName,clazz)
                def unImps = astParser.getUnimplementedMethod(clazz,itfNode)
                if(unImps?.size()>0){
                    String mStr = astParser.methodToString(unImps.get(0));
                    CE.notImplementedMethods(clazz,itfNode,unImps)
                    //fail(CE"unimplemented method:${mStr}",clazz);
                }
            }
        }
        //if(implemented(clazz))
    }
	
	@Override
	public String visit(AstNode node){
		if(node instanceof Statement){
			if(returned) CE.fail("unabled to reach statement",CE.LACKS_OF_STATEMENT,node)
		}
		return super.visit(node)
	}

    @Override
    public String visitCastExpr(CastExpr node) {
        node.type
    } 

    @Override
    public String visitAssignExpr(AssignExpr node) {
        String ft = visit(node.from);
		requireNoneVoid(ft,node)
        String tt = visit(node.to);
		node.from = cast(node.from,ft,tt,node)
        //checkCastable(ft,tt,node);
        return tt
    }

    

    private String getMathType(String t1,String t2,String op){
        def pt1 = castSys.getPrimitiveType(t1) ?: t1
        def pt2 = castSys.getPrimitiveType(t2) ?: t2
        def ret = MathType.getType(pt1,pt2,op)
        return ret
		//return castSys.classifyType(ret)
    }
    
  /*  private ExprNode checkAndCastToBoolean(ExprNode expr){
        String type = visit(expr);
        if(!castSys.isBoolean(type)){
            def be = new BinaryExpr();
            be.expr1 = expr;
            be.operation = "!="
            def zero = new ConstExpr();
            if(castSys.isNumber(type)){
                zero.type = INT_CLASS;
                zero.value = 0;
            }else{
                zero.type = NULL_CLASS;
            }
            be.expr2 = zero;
            return be;
        }
        //TODO cast string to boolean
        return expr;
    }*/
    

    @Override
    public String visitBinaryExpr(BinaryExpr node) {
        String t1 = (visit(node.expr1).toString())
        String t2 = (visit(node.expr2).toString())
        String op = node.operation
        String t;
        switch(op){
        case "==":
            if(castSys.isNumber(t1)){
                if(!castSys.isNumber(t2)) CE.failedToCast(node,t2,INT_CLASS);
                //fail("Number required",node);
            }else{
                //pass anything
            }
            break;
        case '+':
        case '-':
        case '*':
        case '/':
        case '%':
            requireNumber(node,t1)
            requireNumber(node,t2)
            t = getMathType(t1,t2,op);
            break;
        case ">=":
        case "<=":
        case ">":
        case "<":
            requireNumber(node,t1);
            requireNumber(node,t2)
            t = BOOLEAN_CLASS
            break;
        case "&&":
        case "||":
            requireBoolean(node,t1)
            requireBoolean(node,t2)
            t = BOOLEAN_CLASS
            break;
        case "&":
        case "|":
        case "^":
            requireNumber(node,t1);
            requireNumber(node,t2);
            t = castSys.getHigherType(t1,t2)
            break;
        default:
			AstError.fail("unsupport operation:${op}",AstError.UNSUPPORTED,node);
            //throw new TypeError();
        }
        return t;
    }

    @Override
    public String visitConstExpr(ConstExpr node) {
        return node.type
    }

    @Override
    public String visitElementExpr(ElementExpr node) {
        String type = visit(node.target)
        //if(!type.endsWith("[]")){
            requireArray(node,type)
            //fail("Array type required",node)
        //}
        return type.substring(0,type.length()-2)
    }

    @Override
    public String visitFieldExpr(FieldExpr node) {
		if(!node.target){
			def field = fields.get(node.fieldName)
			if(isStatic(method?.modifier)){
				requireStatic(field.modifier,node)
			}
			return field.type
		}
		String t = visit(node.target)
		ClassNode target = this.astLoader.loadAst(t)
		String fname = node.fieldName
		def field = this.astParser.getField(target,fname)
		if(field==null){
			AstError.fieldNotFound(node,fname)
		}
		if(node.target instanceof ClassExpr){
			requireStatic(field.modifier,node)
		}
		return field.type
    }

    @Override
    public String visitInvocationExpr(InvocationExpr node) {
        List<String> types = visit(node.arguments);
        String target = node.target?visit(node.target):this.clazz.name;
        String methodName = node.methodName;
        ClassNode ast = loadAst(target,node);
        MethodNode method = selectMethod(node,ast,methodName,(String[])types.toArray())
        boolean inStaticMethod = node.target==null && Modifier.isStatic(method.modifier)
		boolean isClassExpr = node.target instanceof ClassExpr
		if(inStaticMethod || isClassExpr){
			requireStatic(method.modifier,node)
		}
		castInvocationParams(node,method)
		//TODO here could be optim
		this.exceptionStack.peek().addAll(method.exceptionTypes)
		return method.type
    }

    @Override
    public String visitParameterExpr(ParameterExpr node) {
        node.parameter.type
    }

    @Override
    public String visitUnaryExpr(UnaryExpr node) {
        def preOp = node.preOperation
        String et = visit(node.expr)
        if(preOp=="!"){
            requireBoolean(node,et)
        }
        return et;
    }

    @Override
    public String visitVarExpr(VarExpr node) {
        Integer vid = node.varId;
        def declStmt = this.varDeclStmts.get(vid);
        declStmt?.type
    }
	
	private void caughException(String type){
		List<String> exceptions = this.exceptionStack.peek();
		for(def e in exceptions){
			if(this.castSys.isSubclass(e,type)){
				exceptions.remove(e)
			}
		}
	}

    @Override
	public String visitTryStmt(TryStmt node) {
		this.exceptionStack.add(new LinkedList());
		return super.visitTryStmt(node);
		if(method.exceptionTypes){
			for(e in method.exceptionTypes){
				this.caughException(e)
			}
		}
		List<String> uncaught = this.exceptionStack.pop()
		if(uncaught.size()>0){
			CE.uncaughtException(node,uncaught)
		}
	}

	@Override
	public String visitCatchStmt(CatchStmt node) {
		this.caughException(node.catchVarDecl.type)
		return super.visitCatchStmt(node);
	}

	@Override
    public String visitClassExpr(ClassExpr node) {
        return node.name
    }

    @Override
    public String visitNewExpr(NewExpr node) {
        return node.type
    }

    @Override
    public String visitVarDeclStmt(VarDeclStmt node) {
        //Type infer
        if(node.type==null){
            if(node.initExpr){
                node.type = visit(node.initExpr)
				requireNoneVoid(node.type,node);
            }else{
                node.type = DEFAULT_CLASS
            }
        }
        this.varDeclStmts.put(node.varId,node)
    }
    @Override
    public String visitNewArrayExpr(NewArrayExpr node){
        "${node.type}[]"
    }
    @Override
    public String visitIfStmt(IfStmt node){
        //node.conditionExpr = this.checkAndCastToBoolean(node.conditionExpr);
        this.requireBoolean(node,visit(node.conditionExpr))
		if(node.trueBody) visit(node.trueBody)
		boolean returnedOld = returned
		returned = false
        if(node.falseBody){
			visit(node.falseBody)
        }else{
			returned = true
        }
        returned = returnedOld && returned
		return null;
    }
    
    @Override
	public String visitLoopStmt(LoopStmt node) {
		super.visit(node)
		if(node.preConditionExpr){
			requireBoolean(node.preConditionExpr);
		}
		if(node.postConditionExpr){
			requireBoolean(node.postConditionExpr)
		}
		return null
	}

	@Override
	public String visitMethodNode(MethodNode node) {
		method = node
		returned = false
		this.exceptionStack.push(new LinkedList());
		def ret = super.visitMethodNode(node)
		this.exceptionStack.pop()
		boolean needReturn = (node.type!='void'&&node.type==null)
		if(node.body && needReturn && !returned){
			String mStr = this.astParser.methodToString(node,this.clazz.name)
			CE.fail("Missing return statement in method:${mStr}",CE.LACKS_OF_STATEMENT,node)
		}
		return ret
	}

	@Override
	public String visitReturnStmt(ReturnStmt node) {
		String retType = method.type
		//this.checkCastable(visit(node.expr),retType,node)
		if(node.expr){
			String exType = visit(node.expr)
			node.expr = this.cast(node.expr,exType,retType,node)
		}
		returned = true
		return null
	}

	void requireNumber(AstNode node,String t){
        if(!castSys.isNumber(t)){
            CE.failedToCast(node,t,INT_CLASS)
        }
    }
	
	void requireBoolean(AstNode node){
		String t = visit(node);
		requireBoolean(node,t)
	}
    
    void requireBoolean(AstNode node,String t){
        if(!castSys.isBoolean(t)) CE.failedToCast(node,t,BOOLEAN_CLASS)
    }
    
    boolean isArray(String t){
        return t.endsWith("[]")
    }
    
    void requireArray(AstNode node,String t){
        if(!isArray(t)) CE.failedToCast(node,t,'array')
    }
    
	boolean isStatic(Integer modifier){
		modifier ? Modifier.isStatic(modifier) : false
	}
	
	void requireStatic(Integer modifier,AstNode node){
		boolean isStatic = isStatic(modifier)
		if(!isStatic){
			CE.fail("couldn't refer non-static member in static context",CE.UNSUPPORTED,node)
		}
	}
	
	void requireNoneVoid(String type,AstNode node){
		if(!type || type==VOID_TYPE){
			CE.unsupported("use void type as value",node)
		}
	}
	
	MethodNode selectMethod(AstNode node,ClassNode cls,String methodName,String[] types){
		def methods = this.astParser.getMethodsByName(cls,methodName)
		def matches 
		matches = this.astParser.matchMethodsByType(methods,types,false,false)
		if(!matches){
			matches = this.astParser.matchMethodsByType(methods,types,true,false)
		}
		if(!matches){
			matches = this.astParser.matchMethodsByType(methods,types,true,true)
		}
		List typeList = []
		if(types) typeList.addAll(types)
		if(!matches){
			CE.methodNotFound(node,cls.name,methodName,typeList)
		}	
		if(matches.length>1){
			CE.fail("the method ${methodName} is ambiguous",CE.METHOD_NOT_FOUND,node)
		}
		return matches[0]
	}
	
	private void castInvocationParams(InvocationExpr expr,MethodNode method){
		String[] mTypes = this.astParser.getParameterTypes(method)
		int i=0;
		for(mt in mTypes){
			def pt = visit(expr.arguments[i])
			expr.arguments[i] = this.castSys.cast(expr.arguments[i],pt,mt)
			i++
		}
	}

}