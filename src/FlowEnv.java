import java.util.HashMap;
import java.util.Objects;

import type.CoreTypeRepository;
import type.Type;

import com.google.dart.compiler.resolver.VariableElement;

public class FlowEnv {
  private final /* maybenull */FlowEnv parent;
  private final Type thisType;
  private final Type returnType;
  private final Type expectedType;
  private final HashMap<VariableElement, Type> variableTypeMap;

  private FlowEnv(/* maybenull */FlowEnv parent, /* maybenull */Type thisType, Type returnType, Type expectedType, HashMap<VariableElement, Type> variableTypeMap) {
    this.parent = parent;
    this.thisType = thisType;
    this.returnType = Objects.requireNonNull(returnType);
    this.expectedType = Objects.requireNonNull(expectedType);;
    this.variableTypeMap = variableTypeMap;
  }
  
  public FlowEnv(Type thisType) {
    this(null, thisType, CoreTypeRepository.VOID_TYPE, CoreTypeRepository.VOID_TYPE, null);
  }
  
  /**
   * Create a new flow environment with a parent and an expected type.
   * @param parent
   * @param expectedType
   */
  public FlowEnv(/* maybenull */FlowEnv parent, Type returnType, Type expectedType) {
    this(parent, parent.thisType, returnType, expectedType, new HashMap<VariableElement, Type>());
  }
  
  /**
   * Retrieve the type of a variable.
   * If the current flow environment has no variable registered, this call
   * is delegated to the parent flow environment if it exists.
   * 
   * @param variable a variable.
   * @return the type of the variable or null if the variable is unknown.
   */
  public Type getType(VariableElement variable) {
    System.out.println(variableTypeMap);
    Type type = variableTypeMap.get(variable);
    if (type == null) {
      if (parent != null) {
        return parent.getType(variable);
      }
    }
    return type;
  }

  /**
   * Register a new type for a variable.
   * @param variable a variable
   * @param type a type
   */
  public void register(VariableElement variable, Type type) {
    Objects.requireNonNull(variable);
    Objects.requireNonNull(type);
    variableTypeMap.put(variable, type);
  }
  
  /**
   * Returns the type of '{@code this}'.
   * @return the type of '{@code this}' or null if it doesn't exist.  
   */
  public Type getThisType() {
    return thisType;
  }
  
  /**
   * Returns the declared return type of the current function/method.
   * @return the declared return type of the current function/method.
   */
  public Type getReturnType() {
    return returnType;
  }
  
  /**
   * Create a new flow environment that will share the same variable types with the current one
   * and have a different expected type.
   * @param expectedType new expected type.
   * @return a new flow environment.
   */
  public FlowEnv expectedType(Type expectedType) {
    if (expectedType.equals(this.expectedType)) {  // implicit null check
      return this;
    }
    return new FlowEnv(parent, thisType, returnType, expectedType, variableTypeMap);
  }
  
  /**
   * Returns the current expected type.
   * @return the current expected type.
   */
  public Type getExpectedType() {
    return expectedType;
  }
  
  @Override
  public String toString() {
    return "" + variableTypeMap + ", " + parent;
  }
}
