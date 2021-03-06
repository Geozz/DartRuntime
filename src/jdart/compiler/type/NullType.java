package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

public class NullType implements Type {
  NullType() {
    // enforce singleton
  }

  @Override
  public String toString() {
    return "null";
  }

  @Override
  public boolean isNullable() {
    return true;
  }

  @Override
  public Type asNullable() {
    return this;
  }

  @Override
  public Type asNonNull() {
    throw new IllegalStateException("null type");
  }

  @Override
  public Type asNullable(boolean nullable) {
    return (nullable)?asNullable(): asNonNull();
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitNullType(this, parameter);
  }

  @Override
  public Object asConstant() {
    return NULL_VALUE;
  }

  @Override
  public Type map(TypeMapper typeMapper) {
    Type resultType = typeMapper.transform(this);
    return (resultType == null)? DYNAMIC_NON_NULL_TYPE: resultType;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type.isNullable()) {
      return NULL_TYPE;
    }
    return null;
  }

  @Override
  public Type exclude(Type other) {
    return null;
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
  
  @Override
  public boolean isIncludeIn(Type other) {
    if (other.isNullable()) {
      return true;
    }
    
    if (other instanceof DynamicType) {
      return true;
    }
    
    return false;
  }
  
  @Override
  public boolean isAssignableFrom(Type other) {
    if (other instanceof NullType) {
      return true;
    }
    return false;
  }
}
