import java.util.*;
enum E_TYPE{
  SAME_ARG_NAME,
  ARG_EXIST,
  BODY_REDECLARATION,
  SAME_CLASS_NAME,
  FIELD_EXISTS,
  METHOD_EXISTS,
  EXTEND_NOT_DEFINED,
  OVERRIDE_SUPERCLASS_METHOD,
  CLASS_NOT_EXISTS,
  RETURN_TYPE_NOT_EXISTS,
  ARG_TYPE_NOT_EXISTS,
  BODY_VAR_TYPE_NOT_EXISTS,
  IF_EXPR_NOT_BOOLEAN,
  NOT_INT_ARRAY_LOOKUP,
  NOT_AN_ARRAY,
  NOT_INT_PRINT,
  WHILE_EXPR_NOT_BOOLEAN,
  BAD_TYPES_ASSIGNMENT,
  UNDECLARED_VARIABLE,
  INCOMPATIBLE_PLUS,
  INCOMPATIBLE_COMPARE,
  INCOMPATIBLE_AND,
  INCOMPATIBLE_MINUS,
  INCOMPATIBLE_MULT,
  NO_METHOD_FOR_TYPE,
  UNDECLARED_METHOD,
  INVALID_ARGS_CALL,
  INVALID_RETURN_TYPE,
  NOT_A_CLASS_TYPE
}
public class semanticError{
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_RESET = "\u001B[0m";
  public static String LastClassError;
  public static String LastMethodError;
  static ArrayList<E_TYPE> _error_= new ArrayList<E_TYPE>();
  static ArrayList<String> _method_ = new ArrayList<String>();
  static ArrayList<String> _class_= new ArrayList<String>();;
  public static void addError(E_TYPE t,String m,String c){
    _error_.add(t);
    _method_.add(m);
    _class_.add(c);
    LastClassError=c;
    LastMethodError=m;
  }
  public semanticError(){
    LastClassError=null;
    LastMethodError=null;
    _error_= new ArrayList<E_TYPE>();
    _method_ = new ArrayList<String>();
    _class_= new ArrayList<String>();;
  }
  public static void print(){
    for(int i=0;i<_error_.size();i++){
      System.err.printf(ANSI_RED+"In class '"+_class_.get(i)+"' "+ANSI_RESET);
      if(!(_method_.get(i)).equals(""))
        System.err.printf(ANSI_RED+"in method '"+_method_.get(i)+"' "+ANSI_RESET);
      System.err.printf(ANSI_RED+": ");
      switch(_error_.get(i)){
        case SAME_ARG_NAME:
          System.err.println(ANSI_RED+"There are argument(s) with same name!"+ANSI_RESET);
          break;
        case ARG_EXIST:
          System.err.println(ANSI_RED+"Redeclaration of variable in body (Previously declared in args)!"+ANSI_RESET);
          break;
        case BODY_REDECLARATION:
          System.err.println(ANSI_RED+"Redeclaration of variable in body (Previously declared in body)!"+ANSI_RESET);
          break;
        case SAME_CLASS_NAME:
          System.err.println(ANSI_RED+"Redeclaration of class!"+ANSI_RESET);
          break;
        case FIELD_EXISTS:
          System.err.println(ANSI_RED+"Redeclaration of field!"+ANSI_RESET);
          break;
        case METHOD_EXISTS:
          System.err.println(ANSI_RED+"Redeclaration of method (No overloading accepted)!"+ANSI_RESET);
          break;
        case EXTEND_NOT_DEFINED:
          System.err.println(ANSI_RED+"No previous declaration of Superclass!"+ANSI_RESET);
          break;
        case OVERRIDE_SUPERCLASS_METHOD:
          System.err.println(ANSI_RED+"Method overload (No overloading accepted)!"+ANSI_RESET);
          break;
        case CLASS_NOT_EXISTS:
          System.err.println(ANSI_RED+"Data member type doesn't exist!"+ANSI_RESET);
          break;
        case RETURN_TYPE_NOT_EXISTS:
          System.err.println(ANSI_RED+"Return type doesn't exist!"+ANSI_RESET);
          break;
        case ARG_TYPE_NOT_EXISTS:
          System.err.println(ANSI_RED+"Argument type doesn't exist!"+ANSI_RESET);
          break;
        case BODY_VAR_TYPE_NOT_EXISTS:
          System.err.println(ANSI_RED+"Body variable type doesn't exist!"+ANSI_RESET);
          break;
        case IF_EXPR_NOT_BOOLEAN:
          System.err.println(ANSI_RED+"Expression in if statement not boolean!"+ANSI_RESET);
          break;
        case NOT_INT_ARRAY_LOOKUP:
          System.err.println(ANSI_RED+"Index in int[] not of type int!"+ANSI_RESET);
          break;
        case NOT_AN_ARRAY:
          System.err.println(ANSI_RED+"Variable not an array!"+ANSI_RESET);
          break;
        case NOT_INT_PRINT:
          System.err.println(ANSI_RED+"Print with not int (only int types are allowed in print)!"+ANSI_RESET);
          break;
        case WHILE_EXPR_NOT_BOOLEAN:
          System.err.println(ANSI_RED+"Expression in while statement not boolean!"+ANSI_RESET);
          break;
        case BAD_TYPES_ASSIGNMENT:
          System.err.println(ANSI_RED+"Assignment not of compatible types!"+ANSI_RESET);
          break;
        case UNDECLARED_VARIABLE:
          System.err.println(ANSI_RED+"Undeclared variable!"+ANSI_RESET);
          break;
        case INCOMPATIBLE_PLUS:
          System.err.println(ANSI_RED+"Adding incompatible types!"+ANSI_RESET);
          break;
        case INCOMPATIBLE_COMPARE:
          System.err.println(ANSI_RED+"Comparing incompatible types!"+ANSI_RESET);
          break;
        case INCOMPATIBLE_AND:
          System.err.println(ANSI_RED+"Logical AND between incompatible types!"+ANSI_RESET);
          break;
        case INCOMPATIBLE_MINUS:
          System.err.println(ANSI_RED+"Substracting incompatible types!"+ANSI_RESET);
          break;
        case INCOMPATIBLE_MULT:
          System.err.println(ANSI_RED+"Multiplication between incompatible types!"+ANSI_RESET);
          break;
        case NO_METHOD_FOR_TYPE:
          System.err.println(ANSI_RED+"Type has no such method (Type.method)!"+ANSI_RESET);
          break;
        case UNDECLARED_METHOD:
          System.err.println(ANSI_RED+"Undeclared method call!"+ANSI_RESET);
          break;
        case INVALID_ARGS_CALL:
          System.err.println(ANSI_RED+"Argument types in method call don't match with method declaration!"+ANSI_RESET);
          break;
        case INVALID_RETURN_TYPE:
          System.err.println(ANSI_RED+"Return type doesn't match with method declaration!"+ANSI_RESET);
          break;
        case NOT_A_CLASS_TYPE:
          System.err.println(ANSI_RED+"No such class type in new IDENTIFIER()"+ANSI_RESET);
          break;
      }
    }
  }
  public static boolean ErrorsExist(){
    if(_error_.size()>0)
      return true;
    return false;
  }
}
