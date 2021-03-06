package jdart.compiler.type;

import jdart.compiler.flow.FlowEnv;

public interface Type {
  /**
   * Returns whenever or not the current type allows null.
   * 
   * @return true is the current type is nullable, false otherwise.
   */
  boolean isNullable();

  /**
   * Returns the nullable type of the current type.
   * 
   * @return the nullable type of the current type.
   */
  Type asNullable();

  /**
   * Returns the non null type of the current type.
   * 
   * @return the non null type of the current type.
   */
  Type asNonNull();


  Type asNullable(boolean nullable);

  /**
   * Visitor's accept method (doubble dispatch).
   * 
   * @param visitor
   *          the type visitor.
   * @param parameter
   *          the parameter
   * @return the return value
   */
  <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter);

  /**
   * Returns the constant value or null if the type is not constant. The value
   * {@link NullType#NULL_VALUE} is used to represent the constant value
   * {@code null}.
   * 
   * @return the constant value or null if the type is not constant.
   * 
   * @see #NULL_VALUE
   */
  Object asConstant();

  public static final Object NULL_VALUE = new Object();

  /**
   * Transforms the current type to a new type by applying the mapping
   * specified by the {@link TypeMapper}.
   * If the specified transformation returns {@code null}, then
   * the return type should be the non null dynamic type.
   * 
   * @param typeMapper calculate the transformation from one type to another.
   * @return the new type (never null).
   * 
   * @see UnionType#map(TypeMapper)
   */
  Type map(TypeMapper typeMapper);

  /**
   * Returns common values between this and the other type.
   * 
   * @param type
   *          Type to use to check common values.
   * @return Type containing common values between this and the other type.
   */
  Type commonValuesWith(Type type);

  /**
   * Returns values which are not common between this and the other type.
   * 
   * @param other Type to use to check not common values.
   * @return Type containing not common values between this and the other type.
   */
  Type exclude(Type other);
  
  /**
   * Returns the values of this type which are less or equals than specified Type.
   * 
   * For example (int[5;20]).lessThanOrEqualsValues(int[10; 30]) will return int[5;10].
   * 
   * @param other Type to use.
   * @param inLoop InLoop state of the {@link FlowEnv environment}.
   * @return The values of this type which are less or equals than other Type. Return null if types are not computable.
   */
  Type lessThanOrEqualsValues(Type other, boolean inLoop);

  /**
   * Returns the values of this type which are less than specified Type.
   * 
   * For example (int[5;20]).lessThanValues(int[10; 30]) will return int[5;9].
   * 
   * @param other Type to use.
   * @param inLoop InLoop state of the {@link FlowEnv environment}.
   * @return The values of this type which are less than other Type. Return null if types are not computable.
   */
  Type lessThanValues(Type other, boolean inLoop);

  /**
   * Returns the values of this type which are greater or equals than specified Type.
   * 
   * For example (int[5;20]).greaterThanOrEqualsValues(int[10; 15]) will return int[15;20].
   * 
   * @param other Type to use.
   * @param inLoop InLoop state of the {@link FlowEnv environment}.
   * @return The values of this type which are greater or equals than other Type. Return null if types are not computable.
   */
  Type greaterThanOrEqualsValues(Type other, boolean inLoop);

  /**
   * Returns the values of this type which are greater than specified Type.
   * 
   * For example (int[5;20]).greaterThanValues(int[10; 15]) will return int[16;20].
   * 
   * @param other Type to use.
   * @param inLoop InLoop state of the {@link FlowEnv environment}.
   * @return The values of this type which are greater than other Type. Return null if types are not computable.
   */
  Type greaterThanValues(Type other, boolean inLoop);

  /**
   * Returns <code>true</code> if this type is include in the specified type.
   * 
   * @param other
   *          Reference type.
   * @return <code>true</code> if this type is include in the specified type.
   */
  boolean isIncludeIn(Type other);
  
  boolean isAssignableFrom(Type other);
}
