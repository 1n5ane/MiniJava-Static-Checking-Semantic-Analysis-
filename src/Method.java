import java.util.*;
public class Method{
  String retType;
  String name;
  boolean overrides;
  //linked hashmap because i want to preserve insertion order
  LinkedHashMap<String,Field> args=new LinkedHashMap<String,Field>();
  HashMap<String,Field> bodyVars=new HashMap<String,Field>();

  public Method(String r,String n,boolean rides){
    retType=new String(r);
    name=new String(n);
    overrides=rides;
  }
  public boolean overrides(){
    return overrides;
  }
  public String getType(){
    return retType;
  }
  // the following method check if return type,arg types and bpdyVar types exist
  public int checkVarDecl(){
    if(!(retType.equals("int")||retType.equals("int[]")||retType.equals("boolean")||retType.equals("String[]")||retType.equals("void")||SymbolTable.getClass(retType)!=null))
      semanticError.addError(E_TYPE.RETURN_TYPE_NOT_EXISTS,SymbolTable.currentMethod,SymbolTable.currentClass);
    for (Map.Entry<String, Field> mapElement : args.entrySet()) {
      String key = mapElement.getKey();
      Field f = mapElement.getValue();
      if(!f.typeExists(f.Type()))
        semanticError.addError(E_TYPE.ARG_TYPE_NOT_EXISTS,SymbolTable.currentMethod,SymbolTable.currentClass);
    }
    for (Map.Entry<String, Field> mapElement : bodyVars.entrySet()) {
      String key = mapElement.getKey();
      Field f = mapElement.getValue();
      if(!f.typeExists(f.Type()))
        semanticError.addError(E_TYPE.BODY_VAR_TYPE_NOT_EXISTS,SymbolTable.currentMethod,SymbolTable.currentClass);
    }
    return 0;
  }
  public int methodCmp(String ret,String arguments){
    /*  return 0 if methods have same arg types and same return types*/
    if(retType.equals(ret)){
      if(args.size()>0){
        if(arguments.equals(""))
          return 1;

        String argsArr[]=arguments.split("\\s");
        Field[] f=null;
        if(argsArr.length/2>0)
          f=new Field[argsArr.length/2];
        for (int i=0;i<argsArr.length;i+=2)
          f[i/2]=new Field(argsArr[i],argsArr[i+1]);
        // check if same number of args
        if(argsArr.length/2==args.size()){
          // need to check types
          int i=0;
          for (Map.Entry<String, Field> mapElement : args.entrySet()) {
            String key = mapElement.getKey();
            Field val = mapElement.getValue();
            // System.out.println("************");
            // val.print();
            // f[i].print();

            if(!(f[i].Type()).equals(val.Type()))
              return 1;

            i+=1;
          }
        }else
          return 1;
      }else if(arguments.equals("")){
        return 0;
      }else{
        // different number of arguments
        return 1;
      }
      // System.out.println("***************");
      // System.out.println(arguments);
      // System.out.println(str);
      // System.out.println("***************");
    }else
      return 1;

    return 0;
  }

  public int methodCmpCallAndDecl(String arguments){
    // arguments contain arg types
    if(args.size()>0){
      if(arguments.equals(""))
        return 1;

      String argsArr[]=arguments.split("\\s");
      Field[] f=null;
      if(argsArr.length>0)
        f=new Field[argsArr.length];
      for (int i=0;i<argsArr.length;i++)
        f[i]=new Field(argsArr[i],"dont care");
      // check if same number of args
      if(argsArr.length==args.size()){
        // need to check types
        int i=0;
        for (Map.Entry<String, Field> mapElement : args.entrySet()) {
          String key = mapElement.getKey();
          Field val = mapElement.getValue();
          // val.Type() can be superclass of f[i].Type() -> not the other way around
          /* fro ex.
            class A{}
            class B extends A{}
            class C extends B{
                public int foo(B b){...}
            }
            foo(B)-> OK
            foo(C)-> OK. C IS ALSO B
            foo(A)-> NOT OK. A CANNOT BE B-> ONLY B CAN BE A
            The same applies to return types
                                                                */
          if(!SymbolTable.compatibleTypes(val.Type(),f[i].Type()))
            return 1;

          i+=1;
        }
      }else
        return 1;
    }else if(arguments.equals(""))
      return 0;
    else
      return 1;
    return 0;
  }

  public void print(){
    System.out.println("\u001B[34m"+"\t:METHOD:"+"\u001B[0m");
    System.out.printf("\t\t"+retType+" "+name+"(");
    String outp="";
    for (Field f : args.values()){
      outp+=f.Type()+" "+f.Name()+",";
    }
    if(args.size()>0){
      int l=outp.length()-1;
      char[] tmp=outp.toCharArray();
      tmp[l]=')';
      outp=new String(tmp);
      System.out.printf(outp);
    }else
      System.out.printf(")");
    System.out.println(" "+overrides);
    System.out.println("\u001B[36m"+"\t\t::BODY VARS::"+"\u001B[0m");
    for (Field f : bodyVars.values()){
      System.out.printf("\t\t");
      f.print();
    }
    if(bodyVars.size()==0)
      System.out.println("\t\t  -");
  }
  public void insertArg(Field f){
    if(argsLookup(f.Name())!=null){
      // argument already exists->error
      semanticError.addError(E_TYPE.SAME_ARG_NAME,name,SymbolTable.currentClass);
      return ;
    }
    args.put(f.Name(),f);
  }
  public void insertBodyVar(Field f){
    if(argsLookup(f.Name())!=null){
      // argument already exists->error
      semanticError.addError(E_TYPE.ARG_EXIST,name,SymbolTable.currentClass);
      return ;
    }else if(bodyVarsLookup(f.Name())!=null){
      // argument already declared in body
      semanticError.addError(E_TYPE.BODY_REDECLARATION,name,SymbolTable.currentClass);
      return ;
    }
    bodyVars.put(f.Name(),f);
  }
  public void setRetType(String r){
    retType=new String(r);
  }
  public void setName(String r){
    retType=new String(r);
  }
  public String getRetType(){
    return retType;
  }
  public String getName(){
    return name;
  }
  private String argsLookup(String c){
    Field f=args.get(c);
    if(f==null)
      return null;
    return f.Name();
  }
  private String bodyVarsLookup(String c){
    Field f=bodyVars.get(c);
    if(f==null)
      return null;
    return f.Name();
  }
  public String lookup(String var){
    // first check bodyvars for type of var
    Field f=bodyVars.get(var);
    if(f==null){
      // if not found-> search args;
      f=args.get(var);
      // if not found in args-> return null
      if(f==null)
        return null;
    }
    return f.Type();
  }

}
