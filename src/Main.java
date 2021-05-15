import syntaxtree.*;
import visitor.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length <1){
            System.err.println("Usage: java Main <inputFile> <inputFile> ...");
            System.exit(1);
        }
        for(int i=0;i<args.length;i++){
          FileInputStream fis = null;
          try{
              fis = new FileInputStream(args[i]);
              MiniJavaParser parser = new MiniJavaParser(fis);

              Goal root = parser.Goal();

              MyVisitor eval = new MyVisitor();
              System.out.println("--------------------"+args[i]+"--------------------");
              root.accept(eval, null);
          }
          catch(ParseException ex){
              System.out.println(ex.getMessage());
          }
          catch(FileNotFoundException ex){
              System.err.println(ex.getMessage());
          }
          finally{
              try{
                  if(fis != null) fis.close();
              }
              catch(IOException ex){
                  System.err.println(ex.getMessage());
              }
          }
        }
    }
}


class MyVisitor extends GJDepthFirst<String, Integer>{
  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
   @Override
  public String visit(Goal n, Integer argu) throws Exception {
    // 0 is for type checking
    new SymbolTable();
    new semanticError();
    n.f0.accept(this, 0);
    for ( Node node: n.f1.nodes)
      node.accept(this, 0);
    n.f2.accept(this, 0);

    // SymbolTable.print();
    // CHEECK IF ALL DECLARED VARIABLES EXIST--> a class can have for ex. variable of type A and A could be declared later in the file
    // DO THE COUNTER THING
    SymbolTable.checkVarDecl();
    if(!semanticError.ErrorsExist()){
      // time for type checking
      n.f0.accept(this, 1);
      for ( Node node: n.f1.nodes)
        node.accept(this, 1);
      n.f2.accept(this, 1);
    }
    if(semanticError.ErrorsExist()){
      semanticError.print();
    }else
      System.out.println("\u001B[32m"+"\t\t\t\t\tSUCCESS"+"\u001B[0m");


    SymbolTable.offsetPrint();


    return null;
  }
    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, Integer argu) throws Exception {
        if(argu==0){
          // declarations check
          String classname = n.f1.accept(this, argu);
          String methodnull_val="void";
          String method_name="main";
          String arg_ident = n.f11.accept(this,argu);

          SymbolTable.enter(classname);
          if(!semanticError.ErrorsExist()||!semanticError.LastClassError.equals(classname)){

            String varDecl="";
            for ( Node node: n.f14.nodes)
                varDecl +=node.accept(this, argu);

            SymbolTable.insert(method_name,methodnull_val,"String[] "+arg_ident+" ",varDecl);
          }
        }else{
          // type check
          SymbolTable.moveCurrentClass(n.f1.accept(this, argu));
          SymbolTable.moveCurrentMethod("main");

          for ( Node node: n.f15.nodes)
            node.accept(this, argu);
        }
        return null;
    }
    /**
     * f0 -> Block()
     *       | AssignmentStatement()
     *       | ArrayAssignmentStatement()
     *       | IfStatement()
     *       | WhileStatement()
     *       | PrintStatement()
     */
     @Override
    public String visit(Statement n, Integer argu) throws Exception {
       return n.f0.accept(this, argu);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
     @Override
    public String visit(Block n, Integer argu) throws Exception {
      for ( Node node: n.f1.nodes)
        node.accept(this, argu);

       return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
     @Override
    public String visit(AssignmentStatement n, Integer argu) throws Exception {
       String ident=n.f0.accept(this, argu);
       String ident_type=SymbolTable.lookup(ident);
       if(ident_type.equals("no type"))
        return null;
       String expr_type=n.f2.accept(this, argu);
       if(!SymbolTable.compatibleTypes(ident_type,expr_type))
        semanticError.addError(E_TYPE.BAD_TYPES_ASSIGNMENT,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
       return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
     @Override
    public String visit(ArrayAssignmentStatement n, Integer argu) throws Exception {
      String ident=n.f0.accept(this, argu);
      String ident_type=SymbolTable.lookup(ident);
      if(!ident_type.equals("int[]")){
        semanticError.addError(E_TYPE.NOT_AN_ARRAY,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return null;
      }
      String expr_type=n.f2.accept(this, argu);
      if(!expr_type.equals("int")){
       semanticError.addError(E_TYPE.NOT_INT_ARRAY_LOOKUP,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
       return null;
      }
       expr_type=n.f5.accept(this, argu);
       if(!expr_type.equals("int"))
         semanticError.addError(E_TYPE.BAD_TYPES_ASSIGNMENT,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());

       return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
     @Override
    public String visit(IfStatement n, Integer argu) throws Exception {
       String if_type=n.f2.accept(this, argu);
       if(!if_type.equals("boolean"))
        semanticError.addError(E_TYPE.IF_EXPR_NOT_BOOLEAN,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
       n.f4.accept(this, argu);
       n.f6.accept(this, argu);
       return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
     @Override
    public String visit(WhileStatement n, Integer argu) throws Exception {
       String type=n.f2.accept(this, argu);
       if(!type.equals("boolean"))
        semanticError.addError(E_TYPE.WHILE_EXPR_NOT_BOOLEAN,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
       n.f4.accept(this, argu);
       return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
     @Override
    public String visit(PrintStatement n, Integer argu) throws Exception {
      String type=n.f2.accept(this, argu);
      if(!type.equals("int"))
        semanticError.addError(E_TYPE.NOT_INT_PRINT,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
      return null;
    }

    /**
     * f0 -> AndExpression()
     *       | CompareExpression()
     *       | PlusExpression()
     *       | MinusExpression()
     *       | TimesExpression()
     *       | ArrayLookup()
     *       | ArrayLength()
     *       | MessageSend()
     *       | PrimaryExpression()
     */
     @Override
    public String visit(Expression n, Integer argu) throws Exception {
       return n.f0.accept(this, argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "&&"
     * f2 -> PrimaryExpression()
     */
     @Override
    public String visit(AndExpression n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(!type.equals("boolean")){
        semanticError.addError(E_TYPE.INCOMPATIBLE_AND,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return type;
      }
       type=n.f2.accept(this, argu);
       if(!type.equals("boolean")){
         semanticError.addError(E_TYPE.INCOMPATIBLE_AND,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
     @Override
    public String visit(CompareExpression n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(!type.equals("int")){
        semanticError.addError(E_TYPE.INCOMPATIBLE_COMPARE,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return type;
      }
       type=n.f2.accept(this, argu);
       if(!type.equals("int")){
         semanticError.addError(E_TYPE.INCOMPATIBLE_COMPARE,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "boolean";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
     @Override
    public String visit(PlusExpression n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(!type.equals("int")){
        semanticError.addError(E_TYPE.INCOMPATIBLE_PLUS,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return type;
      }
       type=n.f2.accept(this, argu);
       if(!type.equals("int")){
         semanticError.addError(E_TYPE.INCOMPATIBLE_PLUS,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
     @Override
    public String visit(MinusExpression n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(!type.equals("int")){
        semanticError.addError(E_TYPE.INCOMPATIBLE_MINUS,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return type;
      }
       type=n.f2.accept(this, argu);
       if(!type.equals("int")){
         semanticError.addError(E_TYPE.INCOMPATIBLE_MINUS,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
     @Override
    public String visit(TimesExpression n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(!type.equals("int")){
        semanticError.addError(E_TYPE.INCOMPATIBLE_MULT,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return type;
      }
       type=n.f2.accept(this, argu);
       if(!type.equals("int")){
         semanticError.addError(E_TYPE.INCOMPATIBLE_MULT,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
     @Override
    public String visit(ArrayLookup n, Integer argu) throws Exception {
       String type=n.f0.accept(this, argu);
       if(!type.equals("int[]")){
         semanticError.addError(E_TYPE.NOT_AN_ARRAY,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       type=n.f2.accept(this, argu);
       if(!type.equals("int")){
         semanticError.addError(E_TYPE.NOT_INT_ARRAY_LOOKUP,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
     @Override
    public String visit(ArrayLength n, Integer argu) throws Exception {
       String type=n.f0.accept(this, argu);
       if(!type.equals("int[]")){
         semanticError.addError(E_TYPE.NOT_AN_ARRAY,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
         return type;
       }
       return "int";
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
     @Override
    public String visit(MessageSend n, Integer argu) throws Exception {
      String type= n.f0.accept(this, argu);
      if(type.equals("int")||type.equals("int[]")||type.equals("boolean")){
        semanticError.addError(E_TYPE.NO_METHOD_FOR_TYPE,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        return "no type";
      }
      // lookup for method return type and arg types
      String meth_name=n.f2.accept(this, argu);
      String arg_types=n.f4.present()?n.f4.accept(this,argu):"";

      // need to write lookup method with more args (overload the other lookup)
      // System.out.println("ARGUMENS-> "+arg_types);
      if(!type.equals("no type"))
        type=SymbolTable.methodLookup(type,meth_name,arg_types);

      return type;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
     @Override
    public String visit(ExpressionList n, Integer argu) throws Exception {
      String types="";
      types+=n.f0.accept(this, argu)+" ";
      types+=n.f1.accept(this, argu);
      return types;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
     @Override
    public String visit(ExpressionTail n, Integer argu) throws Exception {
      String types="";
      for ( Node node: n.f0.nodes)
          types+=node.accept(this, argu)+" ";
       return types;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
     @Override
    public String visit(ExpressionTerm n, Integer argu) throws Exception {
      return n.f1.accept(this, argu);
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | NotExpression()
     *       | BracketExpression()
     */
     @Override
    public String visit(PrimaryExpression n, Integer argu) throws Exception {
      String type=n.f0.accept(this, argu);
      if(!(type.equals("int")||type.equals("int[]")||type.equals("boolean")))
        if(type.equals("this"))
          type=SymbolTable.currentClass;
        else{
          // identifier could be class type-> search classes
          ClassSymbolTable tb=SymbolTable.getClass(type);
          // if no such class ->search variables
            if(tb==null)
              type=SymbolTable.lookup(type);
        }
      return type;
    }

    /**
     * f0 -> <INTEGER_LITERAL>
     */
     @Override
    public String visit(IntegerLiteral n, Integer argu) throws Exception {
       return "int";
    }

    /**
     * f0 -> "true"
     */
     @Override
    public String visit(TrueLiteral n, Integer argu) throws Exception {
       return "boolean";
    }

    /**
     * f0 -> "false"
     */
     @Override
    public String visit(FalseLiteral n, Integer argu) throws Exception {
       return "boolean";
    }


    /**
     * f0 -> "this"
     */
     @Override
    public String visit(ThisExpression n, Integer argu) throws Exception {
       return "this";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
     @Override
    public String visit(ArrayAllocationExpression n, Integer argu) throws Exception {

      String type=n.f3.accept(this, argu);
      if(!type.equals("int")){
        // generate error
        semanticError.addError(E_TYPE.NOT_INT_ARRAY_LOOKUP,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
      }
       return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
     @Override
    public String visit(AllocationExpression n, Integer argu) throws Exception {
       String type=n.f1.accept(this, argu);
       ClassSymbolTable tb=SymbolTable.getClass(type);
       // idetifier can only be class type
       if(tb==null){
         // means identifier is not a class type
         semanticError.addError(E_TYPE.NOT_A_CLASS_TYPE,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());

       }
       return type;
    }

    /**
     * f0 -> "!"
     * f1 -> PrimaryExpression()
     */
     @Override
    public String visit(NotExpression n, Integer argu) throws Exception {
      return  n.f1.accept(this, argu);
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
     @Override
    public String visit(BracketExpression n, Integer argu) throws Exception {
       return n.f1.accept(this, argu);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, Integer argu) throws Exception {
      if(argu==0){
        String classname = n.f1.accept(this, argu);

        SymbolTable.enter(classname);
        if(!semanticError.ErrorsExist()||!semanticError.LastClassError.equals(classname)){

          String varDecl="";
          for ( Node node: n.f3.nodes)
              varDecl +=node.accept(this, argu);

          SymbolTable.insert(varDecl);
        }

        for ( Node node: n.f4.nodes)
          node.accept(this, argu);
        return null;
      }else{
        // type checkings
        SymbolTable.moveCurrentClass(n.f1.accept(this, argu));

        // type check
        String type="";
        for ( Node node: n.f4.nodes)
            type +=node.accept(this, argu);
        return null;

      }
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, Integer argu) throws Exception {
      if(argu==0){
        // declarations check
        String classname = n.f1.accept(this, argu);

        SymbolTable.enter(classname,n.f3.accept(this,argu));
        if(!semanticError.ErrorsExist()||!semanticError.LastClassError.equals(classname)){

          String varDecl="";
          for ( Node node: n.f5.nodes)
              varDecl +=node.accept(this, argu);

          SymbolTable.insert(varDecl);

        }

        for ( Node node: n.f6.nodes)
          node.accept(this, argu);
        return null;
      }else{
        // type checking
        SymbolTable.moveCurrentClass(n.f1.accept(this, argu));

        // type check
        String type="";
        for ( Node node: n.f6.nodes)
            type +=node.accept(this, argu);
        return null;
      }
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, Integer argu) throws Exception {
      if(argu==0){
          String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";

          String myType = n.f1.accept(this, argu);
          String myName = n.f2.accept(this, argu);
          String varDecl="";
          for ( Node node: n.f7.nodes)
              varDecl +=node.accept(this, argu);

          SymbolTable.insert(myName,myType,argumentList,varDecl);

          return null;
      }else{
        SymbolTable.moveCurrentMethod(n.f2.accept(this, argu));

        // type check
        for ( Node node: n.f8.nodes)
          node.accept(this, argu);
        String type=n.f10.accept(this, argu);
        // compareReturnType -> checks if type is valid with the return type of current method
        if(!SymbolTable.compatibleTypes(n.f1.accept(this, argu),type)){
          // return expr incompatible with declaration
          semanticError.addError(E_TYPE.INVALID_RETURN_TYPE,SymbolTable.getCurrentMethodName(),SymbolTable.getCurrentClassName());
        }

        return null;
      }
    }
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, Integer argu) throws Exception {
      return n.f0.accept(this,argu)+" "+n.f1.accept(this,argu)+" ";
    }
    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, Integer argu) throws Exception {
        String ret = n.f0.accept(this, argu);

        if (n.f1 != null) {
            ret += n.f1.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, Integer argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, Integer argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += " " + node.accept(this, argu);
        }

        return ret;
    }
    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    @Override
    public String visit(Type n, Integer argu) throws Exception {
      return n.f0.accept(this, argu);
    }
    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, Integer argu) throws Exception{
        String type = n.f0.accept(this, argu);
        String name = n.f1.accept(this, argu);
        return type + " " + name;
    }

    @Override
    public String visit(ArrayType n, Integer argu) {
        return "int[]";
    }
    @Override
    public String visit(BooleanType n, Integer argu) {
        return "boolean";
    }
    @Override
    public String visit(IntegerType n, Integer argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, Integer argu) {
        return n.f0.toString();
    }
}
