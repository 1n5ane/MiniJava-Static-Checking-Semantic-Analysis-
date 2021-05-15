*Ον/μο: Athanasis Lolos* \
*ΑΜ: 1115201700072*

## Σχετικά με τα πεδία του SymbolTable class:
```java
static String currentClass;
static String currentMethod;
// visited contains classes that we already computed the offset
static int fieldOf;
static int methodOf;
static Set<String> visited ;

static LinkedHashMap<String,ClassSymbolTable> classMap;

```
**_curretClass_** : Περιέχει τον τύπο της κλάσσης μέσα στην οποία βρισκόμαστε.\
**_curretMethod_** : Περιέχει το όνομα της μεθόδου στην οποία βρισκόμαστε, μέσα στο _currentClass_

**_fieldOf_** : Counter που χρησιμοποιείται για offsetPrint στα μέλη μιας κλάσης.\
**_methodOf_** : Counter που χρησιμοποιείται για offsetPrint στις μεθόδους μιας κλάσης. \
**_visited_** : Set που περιέχει τους τύπους των κλάσσεων για τις οποίες έχει ήδη τυπωθει offset.

**_classMap_** : Περιέχει τις κλάσσεις με την σειρά που έχουν δηλωθεί.

**Το set _visited_ χρειάζεται, καθώς για το offset print επισκεπτόμαστε τις κλάσσεις μία-μία από την τελευταία δήλωση προς την πρώτη (classMap) και επεκτεινόμαστε αναδρομικά προς τα πάνω (αν η κλάσση extends άλλη κλασση) έως ότου φτάσουμε στην supeClass που δεν έχει άλλο extend και ξεκινάμε να υπολογίζουμε τα offset και add στο _visited_ set (μετα με κάθε return επιστρέφουμε στα παιδία της που συνεχίζουν το άθροισμα των counter και add στο _visited_ set). Έτσι όταν πάμε να επισκεφτόυμε μια κλάσση και υπάρχει στο set, τότε την σκιπάρουμε και δεν ξανατυπώνουμε offset.**

## Σχετικά με τα πεδία του ClassSymbolTable class:

```java
String className;
String extendName;
// we care about order -> for offset print
LinkedHashMap<String,Field> fieldMap;
LinkedHashMap<String,Method> methodMap;
```
**_className_** : Περιέχει το όνομα της κλάσσης (τύπο). \
**_extendName_** : Περιέχει το όνομα της super κλάσσης. Άν δεν υπάρχει super class τότε _null_ (τύπο). \
**_fieldMap_** : Περιέχει τα data members της κλάσσης (κλάσση Field) με τη σειρά που εμφανίζονται στις δηλώσεις. \
**_methodMap_** : Περιέχει τα method members της κλάσσης (κλάσση Method) με τη σειρά που εμφανίζονται στις δηλώσεις.

## Σχετικά με τα πεδία του Field class:

```java
String name;
String type;
```

**_name_** : Περιέχει το όνομα της μεταβλητής τύπου type. \
**_type_** : Περιέχει τον τύπο της μεταβλητής με όνομα name.

## Σχετικά με τα πεδία του Method class:

```java
String retType;
String name;
boolean overrides;
//linked hashmap because i want to preserve insertion order
LinkedHashMap<String,Field> args;
HashMap<String,Field> bodyVars;
```

**_retType_** : Περιέχει τον τύπο επιστρογής της συνάρτησης. \
**_name_** : Περιέχει το όνομα τη συνάρτησης. \
**_overrides_** : _true_ ή _false_ ανάλογα με το αν η method κάνει override μέθοδο κλάσσης πατέρα. \
**_args_** : Περιέχει τα ορίσματα της συνάρτησης με την σειρά που δηλώνονται (τύποι _Field_). \
**_bodyVars_** : Περιέχει τις μεταβλητές της μεθόδου με την σειρά που δηλώνονται στο σώμα της (τύποι _Field_).

#### Για τις παραπάνω κλάσσεις έχουν υλοποιηθεί τα απαραίτητα methods (βλ. κώδικα και σχόλια).

## Σχετικά με το error module (semanticError class)
Η κλάσση αποθηκεύει errors που παράγονται απ'την main και τις μεθόδους και τα κάνει display με τρόπο φιλικό προς τον χρήστη, αφού περάσει όλο το αρχείο. \
**Αξίζει να σημειωθεί πως όταν βρεθεί semantic error, το πρόγραμμα δεν τερματίζει και συνεχίζει να ψάχνει για επόμενα errors.**

Η visit του Goal node καλεί την 1η φορά τις accept με δεύτερο όρισμα 0 (declaration check)
και την 2η φορά τις ξανακαλεί με δεύτερο όρισμα 1 (type checking).

**Είμαι σίγουρος πως το πρόγραμμα δεν κωλωνει πουθενά και βγάζει σωστά αποτελέσματα!**

Αρχεία που παραδόθηκαν: ClassSymbolTable.java, SymbolTable.java, Main.java, Field.java, Method.java, Makefile, semanticError.java
