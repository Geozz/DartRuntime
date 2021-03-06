package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

abstract class NullableType implements Type {
  private final boolean isNullable;
  private ArrayType arrayType; // lazy allocated

  NullableType(boolean isNullable) {
    this.isNullable = isNullable;
  }

  @Override
  public boolean isNullable() {
    return isNullable;
  }

  @Override
  public String toString() {
    return (isNullable() ? "?" : "");
  }

  @Override
  public abstract NullableType asNullable();

  @Override
  public abstract NullableType asNonNull();

  @Override
  public Type asNullable(boolean nullable) {
    return (nullable) ? asNullable() : asNonNull();
  }

  /**
   * Returns the nullable array type with the current type as component type.
   * 
   * @return the nullable array type with the current type as component type.
   */
  public ArrayType asArrayType() {
    if (arrayType != null) {
      return arrayType;
    }
    return arrayType = new ArrayType(true, this, INT32_TYPE, null);
  }

  NullableType merge(NullableType type) {
    Object constant = asConstant();

    if (constant != null && constant.equals(type.asConstant())) {
      return (type.isNullable) ? asNullable() : this;
    }
    if (type instanceof UnionType) {
      return ((UnionType) type).merge(this);
    }
    return UnionType.createUnionType(this, type);
  }

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = typeMapper.transform(this);
    return (resultType == null) ? DYNAMIC_NON_NULL_TYPE : resultType;
  }
  
  @Override
  public Type greaterThanOrEqualsValues(Type other, boolean inLoop) {
    return null;
  }

  @Override
  public Type greaterThanValues(Type other, boolean inLoop) {
    return null;
  }
  
  @Override
  public Type lessThanOrEqualsValues(Type other, boolean inLoop) {
    return null;
  }
  
  @Override
  public Type lessThanValues(Type other, boolean inLoop) {
    return null;
  }
}