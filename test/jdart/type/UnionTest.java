package jdart.type;

import static jdart.type.CoreTypeRepository.*;

import java.math.BigInteger;

import jdart.type.DoubleType;
import jdart.type.IntType;
import jdart.type.NullableType;
import jdart.type.Types;
import jdart.type.UnionType;

import org.junit.Assert;
import org.junit.Test;

public class UnionTest {
  @Test
  public void intUnion() {
    Assert.assertEquals(INT_TYPE, Types.union(INT_TYPE, INT_TYPE));
    Assert.assertEquals(INT_TYPE, Types.union(INT_TYPE, INT_NON_NULL_TYPE));
    Assert.assertEquals(INT_TYPE, Types.union(INT_NON_NULL_TYPE, INT_TYPE));
    Assert.assertEquals(INT_NON_NULL_TYPE, Types.union(INT_NON_NULL_TYPE, INT_NON_NULL_TYPE));
  }
  
  @Test
  public void intRangeUnion() {
    Assert.assertEquals(INT_TYPE, Types.union(INT_TYPE, IntType.constant(BigInteger.ONE)));
    Assert.assertEquals(INT_TYPE, Types.union(IntType.constant(BigInteger.ONE), INT_TYPE));
    Assert.assertEquals(INT_NON_NULL_TYPE, Types.union(INT_NON_NULL_TYPE, IntType.constant(BigInteger.ONE)));
    Assert.assertEquals(INT_NON_NULL_TYPE, Types.union(IntType.constant(BigInteger.ONE), INT_NON_NULL_TYPE));
    
    Assert.assertEquals(IntType.constant(BigInteger.ONE), Types.union(IntType.constant(BigInteger.ONE), IntType.constant(BigInteger.ONE)));
    Assert.assertEquals(UnionType.createUnionType(IntType.constant(BigInteger.ONE), IntType.constant(BigInteger.TEN)),
        Types.union(IntType.constant(BigInteger.ONE), IntType.constant(BigInteger.TEN)));
    Assert.assertEquals(INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO),
        Types.union(INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO), INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ONE)));
    Assert.assertEquals(INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO),
        Types.union(INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO), INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ONE)));
    Assert.assertEquals(INT_TYPE.asTypeLessThan(BigInteger.TEN),
        Types.union(INT_TYPE.asTypeLessThan(BigInteger.ZERO), INT_TYPE.asTypeLessThan(BigInteger.TEN)));
    Assert.assertEquals(INT_NON_NULL_TYPE.asTypeLessThan(BigInteger.TEN),
        Types.union(INT_NON_NULL_TYPE.asTypeLessThan(BigInteger.ZERO), INT_NON_NULL_TYPE.asTypeLessThan(BigInteger.TEN)));
    Assert.assertEquals(INT_TYPE,
        Types.union(INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO), INT_TYPE.asTypeLessOrEqualsThan(BigInteger.ZERO)));
    Assert.assertEquals(INT_NON_NULL_TYPE,
        Types.union(INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO), INT_NON_NULL_TYPE.asTypeLessOrEqualsThan(BigInteger.ZERO)));
  }
  
  @Test
  public void intCloseBoundsInclusion() {
    IntType ten = IntType.constant(BigInteger.TEN);
    IntType eleven = IntType.constant(BigInteger.valueOf(11));
    IntType range = INT_NON_NULL_TYPE.asTypeGreaterOrEqualsThan(BigInteger.TEN).
        asTypeLessOrEqualsThan(BigInteger.valueOf(11));
    Assert.assertEquals(range, Types.union(ten, eleven));
    Assert.assertEquals(range, Types.union(eleven, ten));
  }
  
  @Test
  public void doubleUnion() {
    Assert.assertEquals(DOUBLE_TYPE, Types.union(DOUBLE_TYPE, DOUBLE_TYPE));
    Assert.assertEquals(DOUBLE_TYPE, Types.union(DOUBLE_TYPE, DOUBLE_NON_NULL_TYPE));
    Assert.assertEquals(DOUBLE_TYPE, Types.union(DOUBLE_NON_NULL_TYPE, DOUBLE_TYPE));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, Types.union(DOUBLE_NON_NULL_TYPE, DOUBLE_NON_NULL_TYPE));
  }
  
  @Test
  public void doubleConstUnion() {
    Assert.assertEquals(DoubleType.constant(134.0), Types.union(DoubleType.constant(134.0), DoubleType.constant(134.0)));
    Assert.assertEquals(DOUBLE_TYPE, Types.union(DOUBLE_TYPE, DoubleType.constant(666.0)));
    Assert.assertEquals(DOUBLE_TYPE, Types.union(DoubleType.constant(666.0), DOUBLE_TYPE));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, Types.union(DOUBLE_NON_NULL_TYPE, DoubleType.constant(666.0)));
    Assert.assertEquals(DOUBLE_NON_NULL_TYPE, Types.union(DoubleType.constant(666.0), DOUBLE_NON_NULL_TYPE));
    Assert.assertEquals(UnionType.createUnionType(DoubleType.constant(77.0), DoubleType.constant(134.0)),
        Types.union(DoubleType.constant(77.0), DoubleType.constant(134.0)));
  }
  
  @Test
  public void booleanUnion() {
    Assert.assertEquals(BOOL_TYPE, Types.union(BOOL_TYPE, BOOL_TYPE));
    Assert.assertEquals(BOOL_TYPE, Types.union(BOOL_TYPE, BOOL_NON_NULL_TYPE));
    Assert.assertEquals(BOOL_TYPE, Types.union(BOOL_NON_NULL_TYPE, BOOL_TYPE));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, Types.union(BOOL_NON_NULL_TYPE, BOOL_NON_NULL_TYPE));
  }
  
  @Test
  public void booleanConstUnion() {
    Assert.assertEquals(BOOL_NON_NULL_TYPE, Types.union(TRUE, FALSE));
    Assert.assertEquals(BOOL_NON_NULL_TYPE, Types.union(FALSE, TRUE));
    Assert.assertEquals(BOOL_TYPE, Types.union(TRUE.asNullable(), FALSE));
    Assert.assertEquals(BOOL_TYPE, Types.union(TRUE, FALSE.asNullable()));
    Assert.assertEquals(BOOL_TYPE, Types.union(FALSE.asNullable(), TRUE));
    Assert.assertEquals(BOOL_TYPE, Types.union(FALSE, TRUE.asNullable()));
    Assert.assertEquals(TRUE, Types.union(TRUE, TRUE));
    Assert.assertEquals(FALSE, Types.union(FALSE, FALSE));
    Assert.assertEquals(TRUE.asNullable(), Types.union(TRUE.asNullable(), TRUE));
    Assert.assertEquals(TRUE.asNullable(), Types.union(TRUE, TRUE.asNullable()));
    Assert.assertEquals(FALSE.asNullable(), Types.union(FALSE, FALSE.asNullable()));
  }
  
  @Test
  public void unionMerge() {
    NullableType type1 = INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.ZERO);
    NullableType type2 = INT_TYPE.asTypeGreaterOrEqualsThan(BigInteger.TEN);
    Assert.assertEquals(UnionType.createUnionType(type1, DoubleType.constant(56.2)),
        Types.union(type1, Types.union(DoubleType.constant(56.2), type2)));
    Assert.assertEquals(UnionType.createUnionType(type1, DoubleType.constant(56.2)),
        Types.union(type1, Types.union(type2, DoubleType.constant(56.2))));
    Assert.assertEquals(UnionType.createUnionType(type1, DoubleType.constant(56.2)),
        Types.union(Types.union(DoubleType.constant(56.2), type2), type1));
    Assert.assertEquals(UnionType.createUnionType(type1, DoubleType.constant(56.2)),
        Types.union(Types.union(type2, DoubleType.constant(56.2)), type1));
  }
}