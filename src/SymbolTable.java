import java.util.*;
public class SymbolTable{
  static String currentClass;
  static String currentMethod;
  // visited contains classes that we computed the offset
  static int fieldOf;
  static int methodOf;
  static Set<String> visited ;
  // we want to know the insert order of classes --> helpfull for offset
  // print-> print from the last class to first(extends can occur)
  static LinkedHashMap<String,ClassSymbolTable> classMap;

  public SymbolTable(){
    currentClass=null;
    currentMethod=null;
    fieldOf=0;
    methodOf=0;
    classMap=new LinkedHashMap<String,ClassSymbolTable>();
    visited = new HashSet<String>();
  }
  public static String getCurrentClassName(){
    return currentClass;
  }

  public static String getCurrentMethodName(){
    return currentMethod;
  }

  public static void offsetPrint(){
    List<String> keyList = new ArrayList<String>(classMap.keySet());
    for (int i = keyList.size()-1; i >=0; i--)
      if(!visited.contains(keyList.get(i))){
        ClassSymbolTable c=classMap.get((keyList.get(i)));
        c.offsetPrint();
      }
  }

  public static int enter(String classs){
/*    RETURNS 0 IN CASE OF SUCCESS -> 1 OTHERWISE      */
    ClassSymbolTable tb=SymbolTable.getClass(classs);
    if(tb==null){
      classMap.put(classs,new ClassSymbolTable(classs));
      currentClass=classs;
      currentMethod=null;
      return 0;
    }
    currentClass=classs;
    currentMethod=null;
    // class name already defined
    semanticError.addError(E_TYPE.SAME_CLASS_NAME,"",currentClass);

    return 1;
  }

  public static int enter(String classs,String class_extends){
    ClassSymbolTable tb=SymbolTable.getClass(classs);
    if(tb==null){
      classMap.put(classs,new ClassSymbolTable(classs,class_extends));
      currentClass=classs;
      currentMethod=null;
      return 0;
    }
    // class name already defined
    semanticError.addError(E_TYPE.SAME_CLASS_NAME,"",currentClass);
    return 1;
  }

  // insert in currentClass new methods
  public static int insert(String meth_name,String ret_val,String args,String bodyVars){
    // 0 on success else 1
    ClassSymbolTable tb=SymbolTable.getClass(currentClass);
    // need to check for correctness of method with all the extends;
    //--> NO OVERLOADING ONLY OVERRIDE!
    // if found overloading
    int ret=tb.check_no_overloading(ret_val,meth_name,args);
    if(ret==1)
      return 1;
    boolean overrides=false;
    if(ret==2)
      overrides=true;
    if(tb.enter(meth_name,ret_val,overrides)!=0)
      return 1;


    tb.insert(args,bodyVars);
    return 0;
  }
  // insert in currentClass new fields
  public static int insert(String fields){
    // 0 on success else 1
    ClassSymbolTable tb=SymbolTable.getClass(currentClass);
    if(tb.insert(fields)!=0){
      return 1;
    }
    return 0;
  }

  // the following function checks if all variable declaration types exist
  public static int checkVarDecl(){
    for (Map.Entry<String, ClassSymbolTable> mapElement : classMap.entrySet()) {
      String key = mapElement.getKey();
      ClassSymbolTable c = mapElement.getValue();
      currentClass=key;
      // check var decl in data members and all methods
      c.checkVarDecl();
    }
    return 0;
  }

  // returns 0 in case of success
  public static int moveCurrentClass(String c){
    ClassSymbolTable tb=SymbolTable.getClass(c);
    if(tb!=null){
      currentClass=c;
      return 0;
    }
    // class name does not exist
    return 1;
  }

  public static int moveCurrentMethod(String m){
    ClassSymbolTable tb=SymbolTable.getClass(currentClass);
    if(tb.getMethod(m)!=null){
      currentMethod=m;
      return 0;
    }
    // class name does not exist
    return 1;
  }

  public static ClassSymbolTable getClass(String c){
    return SymbolTable.classMap.get(c);
  }

  public static boolean compatibleTypes(String father_t,String child_t){
    // if types are the same
    if(father_t.equals(child_t))
      return true;
      // at first not cool
    boolean cool=false;
    ClassSymbolTable tb=SymbolTable.getClass(child_t);
    if(tb==null)
      return cool;
    while(tb.getExtendName()!=null){
      tb=SymbolTable.getClass(tb.getExtendName());
      if(father_t.equals(tb.getClassName())){
        cool=true;
        break;
      }
    }
    return cool;
  }

  // lookup will search from curent's class,current's method fields and go backward until first occurrence of var
  // it returns type of var
  public static String lookup(String var){
    ClassSymbolTable tb=getClass(currentClass);
    String type=tb.lookup(var);
    if(type==null){
      // implicit generate type tha doesn't exist so that caller can generate error
      return "no type";
    }
    return type;
  }

  public static String methodLookup(String callType,String meth_name,String arg_types){
    ClassSymbolTable tb=getClass(callType);
    String type=tb.methodLookup(meth_name,arg_types);
    if(type==null){
      // implicit generate type that doesn't exist so that caller can generate error
      return "no type";
    }
    return type;
  }

  public static void print(){
    System.out.println("Current class: "+currentClass);
    System.out.println("Current method: "+SymbolTable.currentMethod);
    for (ClassSymbolTable t : classMap.values())
      t.print();
  }
}
