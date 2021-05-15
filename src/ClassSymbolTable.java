import java.util.*;
public class ClassSymbolTable{
  String className;
  String extendName;
  // we care about order -> for offset print
  LinkedHashMap<String,Field> fieldMap=new LinkedHashMap<String,Field>();
  LinkedHashMap<String,Method> methodMap=new LinkedHashMap<String,Method>();
  public ClassSymbolTable(String n){
    className=new String(n);
    extendName=null;
  }

  public ClassSymbolTable(String n,String extends_name){
    className=new String(n);
    if(SymbolTable.getClass(extends_name)==null){
      semanticError.addError(E_TYPE.EXTEND_NOT_DEFINED,"",className);
      return ;
    }
    this.setExtendName(extends_name);
  }

  private void setExtendName(String e){
    extendName=new String(e);
  }

  public String getClassName(){
    return className;
  }

  public String getExtendName(){
    return extendName;
  }

  public Method getMethod(String m){
    return methodMap.get(m);
  }

  public void offsetPrint(){
    if(extendName==null){
      // start printing
      SymbolTable.visited.add(className);
      // initialize counters
      SymbolTable.fieldOf=0;
      SymbolTable.methodOf=0;
      // this is for field offset
      for (Map.Entry<String, Field> mapElement : fieldMap.entrySet()) {
        Field f= mapElement.getValue();
        System.out.println(className+"."+f.Name()+" : "+SymbolTable.fieldOf);
        if(f.Type().equals("int"))
          SymbolTable.fieldOf+=4;
        else if(f.Type().equals("boolean"))
          SymbolTable.fieldOf+=1;
        else
          SymbolTable.fieldOf+=8;
      }
      // this is for method offset
      for (Map.Entry<String, Method> mapElement : methodMap.entrySet()) {
        Method m= mapElement.getValue();
        // if no override
        if(!m.overrides()){
          System.out.println(className+"."+m.getName()+" : "+SymbolTable.methodOf);
          SymbolTable.methodOf+=8;
        }
      }
      return ;
    }
    // add class name to set
    ClassSymbolTable c=SymbolTable.getClass(extendName);
    c.offsetPrint();
    // add current class to visited
    SymbolTable.visited.add(className);
    // this is for field offset
    for (Map.Entry<String, Field> mapElement : fieldMap.entrySet()) {
      Field f= mapElement.getValue();
      System.out.println(className+"."+f.Name()+" : "+SymbolTable.fieldOf);
      if(f.Type().equals("int"))
        SymbolTable.fieldOf+=4;
      else if(f.Type().equals("boolean"))
        SymbolTable.fieldOf+=1;
      else
        SymbolTable.fieldOf+=8;
    }
    // this is for method offset
    for (Map.Entry<String, Method> mapElement : methodMap.entrySet()) {
      Method m= mapElement.getValue();
      // if no override
      if(!m.overrides()){
        System.out.println(className+"."+m.getName()+" : "+SymbolTable.methodOf);
        SymbolTable.methodOf+=8;
      }
    }

  }

  // the following function checks if all var types exist (in fields and methods)
  public int checkVarDecl(){
    // first check all data members
    for (Map.Entry<String, Field> mapElement : fieldMap.entrySet()) {
      String key = mapElement.getKey();
      Field c = mapElement.getValue();
      if(!c.typeExists(c.Type()))
        semanticError.addError(E_TYPE.CLASS_NOT_EXISTS,"",SymbolTable.currentClass);
    }
    // then check all method vars-> args and body declarations
    for (Map.Entry<String, Method> mapElement : methodMap.entrySet()) {
      String key = mapElement.getKey();
      Method m= mapElement.getValue();
      SymbolTable.currentMethod=key;
      m.checkVarDecl();
    }
    return 0;
  }

  public int check_no_overloading(String retType,String methName,String args){
    if(extendName==null){
      return 0;// no override
    }
    return this.checkSuperclass(extendName,retType,methName,args);

  }

  // checkSuperclass checks recursively if each father has the same function -> NO OVERLOADING
  private int checkSuperclass(String superClass,String retType,String methName,String args){
    ClassSymbolTable parent=SymbolTable.getClass(superClass);
    // first search fro method with same name
    Method m=parent.methodMap.get(methName);
    if(m!=null){
      // if found method with same name
      // the method should have same return type and same arguments(number of args and types)

      if(m.methodCmp(retType,args)!=0){
        // it means overloading--> generate error
        semanticError.addError(E_TYPE.OVERRIDE_SUPERCLASS_METHOD,methName,className);
        return 1;
      }
      return 2;
    }
    if(parent.extendName==null)
      return 0;

    int ret=this.checkSuperclass(parent.extendName,retType,methName,args);
    if(ret!=1 &&ret!=0 )
      ret=2;
      // ret 2 means override
    return ret;
  }

  public int enter(String meth_name,String retVal,boolean rides){
  // return 0 in case of success --> 1 otherwise
    Method me=methodMap.get(meth_name);
    if(me==null){
      methodMap.put(meth_name,new Method(retVal,meth_name,rides));
      SymbolTable.currentMethod=new String(meth_name);
      return 0;
    }
    // method already exists->
    semanticError.addError(E_TYPE.METHOD_EXISTS,"",className);
    return 1;
  }

    public int insert(String args,String bodyVars){
    // insert in currentMethod args and bodyvars
      // get currentMethod
      Method curMeth=methodMap.get(SymbolTable.currentMethod);
      if(!args.equals("")){
        String argsArr[]=args.split("\\s");
        Field[] f=null;
        if(argsArr.length/2>0)
          f=new Field[argsArr.length/2];
        for (int i=0;i<argsArr.length;i+=2){
          f[i/2]=new Field(argsArr[i],argsArr[i+1]);
          curMeth.insertArg(f[i/2]);
        }
      }
      if(!bodyVars.equals("")){
        String varDeclArr[]=bodyVars.split("\\s");
        Field[] f=null;
        if(varDeclArr.length/2>0)
          f=new Field[varDeclArr.length/2];
        for (int i=0;i<varDeclArr.length;i+=2){
          f[i/2]=new Field(varDeclArr[i],varDeclArr[i+1]);
          curMeth.insertBodyVar(f[i/2]);
        }
      }

    return 0;
  }


  public int insert(String fie){
    if(!fie.equals("")){
      String fieldArr[]=fie.split("\\s");
      Field[] f=null;
      if(fieldArr.length/2>0)
        f=new Field[fieldArr.length/2];
      for (int i=0;i<fieldArr.length;i+=2){
        f[i/2]=new Field(fieldArr[i],fieldArr[i+1]);
        if(fieldMap.get(f[i/2].Name())==null){
          fieldMap.put(f[i/2].Name(),f[i/2]);
          continue;
        }
        // field already exists-> redeclaration error
        semanticError.addError(E_TYPE.FIELD_EXISTS,"",className);
      }
    }
  return 0;
  }

  public void print(){
    System.out.println("CLASS_NAME: "+className);
    if(extendName!=null)
      System.out.println("EXTENDS: "+extendName);

    System.out.println("\t:FIELDS:");
    for (Field t : fieldMap.values()){
      System.out.printf("\t\t");
      t.print();
    }
    System.out.println("\t:METHODS:");
    for (Method mm :methodMap.values()){
      System.out.printf("\t");
      mm.print();
    }

  }

  // recursive check current class and all superclass and return type of first occurence var
  public String recursiveFieldLookup(String var){
    Field f=fieldMap.get(var);
    if(f==null){
      // if not found in current field-> check extendName
      if(extendName==null)
        return null;
      ClassSymbolTable tb=SymbolTable.getClass(extendName);
      return tb.recursiveFieldLookup(var);
    }
    return f.Type();
  }

  public String lookup(String var){
    Method cur_meth=this.getMethod(SymbolTable.currentMethod);
    String type=cur_meth.lookup(var);

    if(type==null){
    //if not found in method (body or args)
    // search class fields-> recusive (must go to all the superclasses)
      type=this.recursiveFieldLookup(var);
    }
    if(type==null){
      // means variable not found anywhere-> generate error
      semanticError.addError(E_TYPE.UNDECLARED_VARIABLE,SymbolTable.currentMethod,SymbolTable.currentClass);
      return null;
    }
    return type;
  }

  public String methodLookup(String meth_name,String arg_types){
    Method cur_meth=this.findMethod(className,meth_name);

    if(cur_meth==null){
      // means method not found anywhere-> generate error
      semanticError.addError(E_TYPE.UNDECLARED_METHOD,SymbolTable.currentMethod,SymbolTable.currentClass);
      return null;
    }
    // check if arg types are correct
    String ret_type=cur_meth.getType();
    if(cur_meth.methodCmpCallAndDecl(arg_types)!=0){
      // invalid args types
      semanticError.addError(E_TYPE.INVALID_ARGS_CALL,SymbolTable.currentMethod,SymbolTable.currentClass);
    }
    return ret_type;
  }

  // findMethod is called by methodLookup to find the first occurence of meth_name(recursive check to superclass)
  private Method findMethod(String class_name,String meth_name){
    if(class_name==null)
      return null;
    ClassSymbolTable tb=SymbolTable.getClass(class_name);
    Method m=tb.methodMap.get(meth_name);
    if(m==null)
      return findMethod(tb.extendName,meth_name);
    return m;
  }

}
