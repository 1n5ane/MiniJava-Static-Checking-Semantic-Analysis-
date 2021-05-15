import java.util.*;
public class Field{
  String name;
  String type;
  public Field(String t,String n){
    name= new String(n);
    type=new String(t);
  }
  public String Name(){
    return name;
  }
  public String Type(){
    return type;
  }
  public boolean typeExists(String t){
    // String[] only exists in main argument
    if(t.equals("int")||t.equals("int[]")||t.equals("String[]")||t.equals("boolean")||SymbolTable.getClass(t)!=null)
      return true;
    return false;
  }
  public void setType(String t){
    type=new String(t);
  }
  public void setName(String n){
    name=new String(n);
  }
  public void print(){
    System.out.println(type+" "+name);
  }
}
