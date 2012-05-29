package jdart.compiler.type;

import static jdart.compiler.type.CoreTypeRepository.*;

import java.math.BigInteger;
import java.util.Objects;

import com.google.dart.compiler.resolver.ClassElement;

public class IntType extends PrimitiveType {
  private final BigInteger minBound;
  private final BigInteger maxBound;

  IntType(boolean nullable, /* maybenull */BigInteger minBound, /* maybenull */
      BigInteger maxBound) {
    super(nullable);
    this.minBound = minBound;
    this.maxBound = (Objects.equals(minBound, maxBound)) ? minBound : maxBound;
    // be sure that if the type is constant min == max
  }

  public static IntType constant(BigInteger constant) {
    Objects.requireNonNull(constant);
    return new IntType(false, constant, constant);
  }

  @Override
  public int hashCode() {
    return (isNullable() ? 1 : 0) ^ Objects.hashCode(minBound) ^ Integer.rotateLeft(Objects.hashCode(maxBound), 16);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IntType)) {
      return false;
    }
    IntType intType = (IntType) obj;
    return isNullable() == intType.isNullable() && Objects.equals(minBound, intType.minBound) && Objects.equals(maxBound, intType.maxBound);
  }

  @Override
  ClassElement getLazyElement() {
    return CoreTypeRepository.getCoreTypeRepository().getIntClassElement();
  }

  @Override
  public String toString() {
    return "int" + super.toString() + " [" + infinity('-', minBound) + ',' + infinity('+', maxBound) + ']';
  }

  private static String infinity(char sign, BigInteger value) {
    return (value == null) ? sign + "infinity" : value.toString();
  }

  /**
   * Return the minimum bound or null if the bound is -Infinity.
   * 
   * @return the minimum bound.
   */
  public/* maybenull */BigInteger getMinBound() {
    return minBound;
  }

  /**
   * Return the maximum bound or null if the bound is +Infinity.
   * 
   * @return the maximum bound.
   */
  public/* maybenull */BigInteger getMaxBound() {
    return maxBound;
  }

  public boolean isMinBoundInfinity() {
    return minBound == null;
  }

  public boolean isMaxBoundInfinity() {
    return maxBound == null;
  }

  @Override
  public IntType asNullable() {
    if (isNullable()) {
      return this;
    }
    if (minBound == null && maxBound == null) {
      return INT_TYPE;
    }
    return new IntType(true, minBound, maxBound);
  }

  @Override
  public IntType asNonNull() {
    if (!isNullable()) {
      return this;
    }
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  @Override
  public <R, P> R accept(TypeVisitor<? extends R, ? super P> visitor, P parameter) {
    return visitor.visitIntType(this, parameter);
  }

  @Override
  public BigInteger asConstant() {
    if (minBound == maxBound) {
      return minBound;
    }
    return null;
  }

  @Override
  NullableType merge(NullableType type) {
    if (type == INT_TYPE) {
      return INT_TYPE;
    }
    if (type == INT_NON_NULL_TYPE) {
      return (isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    if (!(type instanceof IntType)) {
      return super.merge(type);
    }
    if (this == INT_TYPE) {
      return INT_TYPE;
    }
    if (this == INT_NON_NULL_TYPE) {
      return (type.isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }

    // test inclusion
    IntType intType = (IntType) type;

    if (maxBound != null && intType.minBound != null && maxBound.add(BigInteger.ONE).compareTo(intType.minBound) < 0) {
      // no intersection
      return UnionType.createUnionType(this, intType);
    }

    if (minBound != null && intType.maxBound != null && intType.maxBound.add(BigInteger.ONE).compareTo(minBound) < 0) {
      // no intersection
      return UnionType.createUnionType(intType, this);
    }

    BigInteger min = (minBound == null || intType.minBound == null) ? null : minBound.compareTo(intType.minBound) < 0 ? minBound : intType.minBound;
    BigInteger max = (maxBound == null || intType.maxBound == null) ? null : maxBound.compareTo(intType.maxBound) > 0 ? maxBound : intType.maxBound;
    boolean nullable = isNullable() || intType.isNullable();
    if (min == null && max == null) {
      return (isNullable()) ? INT_TYPE : INT_NON_NULL_TYPE;
    }
    return new IntType(nullable, min, max);
  }

  public DoubleType asDouble() {
    if (minBound != null && minBound == maxBound) {
      DoubleType type = DoubleType.constant(minBound.doubleValue());
      return (isNullable()) ? type.asNullable() : type;
    }
    return (isNullable()) ? DOUBLE_TYPE : DOUBLE_NON_NULL_TYPE;
  }

  //
  // int[min, max] x = ...
  // if (x <= value) {
  //
  public/* maybenull */IntType asTypeLessOrEqualsThan(BigInteger value) {
    if (maxBound == null || value.compareTo(maxBound) <= 0) {
      if (minBound != null && value.compareTo(minBound) < 0) {
        return null;
      }
      return new IntType(isNullable(), minBound, value);
    }
    return this;
  }

  //
  // int[min, max] x = ...
  // if (x < value) {
  //
  public/* maybenull */IntType asTypeLessThan(BigInteger value) {
    return asTypeLessOrEqualsThan(value.add(BigInteger.ONE));
  }

  //
  // int[min, max] x = ...
  // if (x >= value) {
  //
  public/* maybenull */IntType asTypeGreaterOrEqualsThan(BigInteger value) {
    if (minBound == null || value.compareTo(minBound) >= 0) {
      if (maxBound != null && value.compareTo(maxBound) > 0) {
        return null;
      }
      return new IntType(isNullable(), value, maxBound);
    }
    return this;
  }

  //
  // int[min, max] x = ...
  // if (x > value) {
  //
  public/* maybenull */IntType asTypeGreaterThan(BigInteger value) {
    return asTypeGreaterOrEqualsThan(value.subtract(BigInteger.ONE));
  }

  public IntType add(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.add(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.add(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  public IntType sub(IntType type) {
    BigInteger minBound = (this.minBound == null | type.minBound == null) ? null : this.minBound.subtract(type.minBound);
    BigInteger maxBound = (this.maxBound == null | type.maxBound == null) ? null : this.maxBound.subtract(type.maxBound);
    if (minBound == null && maxBound == null) {
      return INT_NON_NULL_TYPE;
    }
    return new IntType(false, minBound, maxBound);
  }

  /*
   * public IntType mul(IntType type) { BigInteger minBound = (this.minBound ==
   * null | type.minBound == null)? null: this.minBound.multiply(type.minBound);
   * BigInteger maxBound = (this.maxBound == null | type.maxBound == null)?
   * null: this.maxBound.multiply(type.maxBound); if (minBound == null &&
   * maxBound == null) { return INT_NON_NULL_TYPE; } return new IntType(false,
   * minBound, maxBound); }
   */

  /**
   * Returns <code>true</code> if this type is include in the specified type.
   * 
   * @param intType
   *          Reference type.
   * @return <code>true</code> if this type is include in the specified type.
   */
  public boolean isIncludeIn(IntType intType) {
    if (minBound == null && intType.minBound != null) {
      return false;
    }

    if (maxBound == null && intType.maxBound != null) {
      return false;
    }
    boolean min = false;
    if (intType.minBound == null || intType.minBound.compareTo(minBound) <= 0) {
      min = true;
    }
    boolean max = false;
    if (intType.maxBound == null || intType.maxBound.compareTo(maxBound) >= 0) {
      max = true;
    }

    return min && max;
  }

  @Override
  public Type commonValuesWith(Type type) {
    if (type instanceof IntType) {
      return intersect(this, (IntType) type);
    }

    if (type instanceof DoubleType) {
      DoubleType dType = (DoubleType) type;
      double constant = dType.asConstant().doubleValue();

      if  ( ((int)constant) == constant) {
        BigInteger valueOfCst = BigInteger.valueOf((long) constant);
        return intersect(new IntType(isNullable() && type.isNullable(), valueOfCst, valueOfCst), this);
      }
      return null;
    }

    if (type instanceof UnionType) {
      return type.commonValuesWith(this);
    }

    return null;
  }

  /**
   * Returns the intersection of type1 and type2.
   * 
   * @param type1
   * @param type2
   * @return The intersection of type1 and type2. Or null if two ranges doens't intersect.
   */
  public static IntType intersect(IntType type1, IntType type2) {
    int diff = diff(type1, type2);
    
    if (diff == -2 || diff == 2) {
      return null;
    }
    
    if (diff == -3) {
      return type2;
    }
    
    if (diff == 3) {
      return type1;
    }
    
    if (diff == 0) {
      return type1;
    }
    
    BigInteger min;
    BigInteger max;
    if (diff == -1) {
      min = type2.minBound;
      max = type1.maxBound;
    } else { // diff == 1
      min = type1.minBound;
      max = type2.maxBound;
    }

    return new IntType(type1.isNullable() && type2.isNullable(), min, max);
  }


  private static int diff(IntType type1, IntType type2) {
    int tmp = diffHelper(type1, type2);
    if (tmp != 0) {
      return -tmp;
    }
    
    tmp = diffHelper(type2, type1);
    if (tmp != 0) {
      return tmp;
    }

    if (type1.minBound == null && type2.minBound == null && type1.maxBound == null && type2.maxBound == null) {
      return 0;
    }
    throw new IllegalStateException();
  }

  private static int diffHelper(IntType type1, IntType type2) {
    if (type2.minBound != null) {
      if (type2.maxBound != null) {
        if (type1.minBound == null || type1.minBound.compareTo(type2.minBound) < 0) {
          if (type1.maxBound == null || type1.maxBound.compareTo(type2.maxBound) < 0) {
            return 3;
          }
        }
      }
      if (type1.maxBound != null) {
        if (type1.maxBound.compareTo(type2.minBound) < 0) {
          return 2;
        } else {
          return 1;
        }
      }
    }
    return 0;
  }

  /**
   * Returns <code>true</code> if this type as common values with the specified
   * type.
   * 
   * @param other
   *          Type to check.
   * @return <code>true</code> if this type as common values with the specified
   *         type.
   */
  public boolean hasCommonValuesWith(IntType other) {
    if (minBound == null) {
      // min == -inf
      if (maxBound == null || other.minBound == null || maxBound.compareTo(other.minBound) < 0) {
        // max == +inf || other.min == inf || max < other.min
        return true;
      }
      return false;
    } else {
      // min != -inf
      if (other.maxBound == null) {
        // oher.max == +inf
        if (maxBound != null) {
          // max != +inf
          if (other.minBound == null || maxBound.compareTo(other.minBound) > 0) {
            // other.min == inf || max > other.min
            return true;
          }
          return false;
        }
        // max == +inf
        return true;
      } else {
        // min != -inf
        // other.max != +inf
        if (minBound.compareTo(other.maxBound) > 0) {
          return false;
        }
        return true;
      }
    }
  }

  /**
   * Remove type range to this range.
   * 
   * For example if this range is [10; 20] ad type range is [12; 17], the return
   * range will be union([10; 12], [17; 20])
   * 
   * @param type
   *          Element to remove
   * @return A new type with the right range. Or null if the range is null.
   */
  public Type exclude(IntType type) {
    BigInteger min = null;
    BigInteger max = null;
    boolean nullable = isNullable() || type.isNullable();

    if (minBound == null) {
      if (maxBound == null) {
        if (type.minBound == null) {
          if (type.maxBound == null) {
            // [-inf;+inf] and [-inf;+inf]
            return null;
          }
          // [-inf;+inf] and [-inf;i]
          min = type.maxBound.add(BigInteger.ONE);
          return new IntType(nullable, null, null);
        }
        // [-inf;+inf] and [i;?]
        if (type.maxBound != null) {
          // [-inf;+inf] and [i;j]
          max = type.minBound.subtract(BigInteger.ONE);
          IntType tmp1 = new IntType(nullable, null, max);

          min = type.maxBound.add(BigInteger.ONE);
          IntType tmp2 = new IntType(nullable, min, null);
          return Types.union(tmp1, tmp2);
        }
        // [-inf;+inf] and [i;+inf]
        max = type.minBound.subtract(BigInteger.ONE);
        return new IntType(nullable, null, max);
      }

      return minBoundIsNull(type, nullable);
    }
    return minBoundIsNotNull(type, nullable);
  }

  private Type minBoundIsNull(IntType type, boolean nullable) {
    BigInteger min;
    BigInteger max;
    // [-inf;i] and [?;?]
    if (type.minBound == null && type.maxBound == null) {
      // [-inf;i] and [-inf;+inf]
      return null;
    }

    if (type.minBound != null) {
      if (type.minBound.compareTo(maxBound) > 0) {
        // [-inf;i] and [j;?] where j > i
        return this;
      }

      if (type.minBound.compareTo(maxBound) <= 0) {
        // [-inf;i] and [j;?] where j <= i
        if (type.maxBound != null && type.maxBound.compareTo(maxBound) > 0) {
          // [-inf;i] and [j;k] where j <= i and k > i
          max = type.minBound.subtract(BigInteger.ONE);
          return new IntType(nullable, null, max);
        }
        // [-inf;i] and [j;k] where j <= i and k <= i
        max = type.minBound.subtract(BigInteger.ONE);
        IntType tmp1 = new IntType(nullable, null, max);

        min = type.maxBound.add(BigInteger.ONE);
        max = maxBound;
        IntType tmp2 = new IntType(nullable, min, max);
        return Types.union(tmp1, tmp2);
      }

      if (type.maxBound == null) {
        // [-inf;i] and [j;+inf]
        max = type.minBound.subtract(BigInteger.ONE);
        return new IntType(nullable, null, max);
      }
    }

    // [-inf;i] and [-inf;j]
    min = type.maxBound.add(BigInteger.ONE);
    max = maxBound;
    if (min.compareTo(max) == 0) {
      // [-inf;i] and [-inf;j] where j > i
      return null;
    }
    return new IntType(nullable, min, max);
  }

  private Type minBoundIsNotNull(IntType type, boolean nullable) {
    BigInteger min;
    BigInteger max;
    // [i;?] [?;?]
    if (maxBound == null) {
      return minBoundNotNullMaxBoundNull(type, nullable);
    }
    // [i;j] [?;?]

    if (type.minBound == null && type.maxBound == null) {
      // [i;j] [-inf;+inf]
      return null;
    }

    if (type.minBound != null && type.maxBound != null) {
      // [i;j] [k;l]
      if (type.maxBound.compareTo(minBound) < 0) {
        // [i;j] [k;l] && l < i
        return this;
      }
      if (type.minBound.compareTo(maxBound) > 0) {
        // [i;j] [k;l] && j < k
        return this;
      }
      if (type.maxBound.compareTo(minBound) > 0) {
        // [i;j] [k;l] && l > i
        if (type.minBound.compareTo(minBound) < 0) {
          // [i;j] [k;l] && l > i && k < i
          min = type.maxBound.add(BigInteger.ONE);
          return new IntType(nullable, min, maxBound);
        }
      }
      if (type.minBound.compareTo(maxBound) < 0) {
        // [i;j] [k;l] && k < j
        if (type.maxBound.compareTo(maxBound) < 0) {
          // [i;j] [k;l] && k < j && l < j
          max = type.minBound.subtract(BigInteger.ONE);
          IntType tmp1 = new IntType(nullable, minBound, max);

          min = type.maxBound.add(BigInteger.ONE);
          IntType tmp2 = new IntType(nullable, min, maxBound);

          return Types.union(tmp1, tmp2);
        }
        // [i;j] [k;l] && k < j && l >= j
        max = type.maxBound.subtract(BigInteger.ONE);
        return new IntType(nullable, minBound, max);
      }
      // [i;j] [k;l] && k >= j
      return this;
    }

    if (type.minBound == null) {
      // [i;j] [-inf;l]
      if (type.maxBound.compareTo(minBound) < 0) {
        // [i;j] [-inf;l] && l < i
        return this;
      }
      if (type.maxBound.compareTo(maxBound) < 0) {
        // [i;j] [-inf;l] & l < j
        min = type.maxBound.add(BigInteger.ONE);
        return new IntType(nullable, min, maxBound);
      }
      // [i;j] [-inf;l] && l >= j
      return null;
    }

    if (type.maxBound == null) {
      // [i;j] [k;+inf]
      if (type.minBound.compareTo(maxBound) > 0) {
        // [i;j] [k;+inf] && k > j
        return this;
      }
      if (type.minBound.compareTo(minBound) > 0) {
        // [i;j] [k;+inf] && k > i
        max = type.minBound.subtract(BigInteger.ONE);
        return new IntType(nullable, minBound, max);
      }
      // [i;j] [k;+inf] && k <= i
      return null;
    }
    return type;
  }

  private Type minBoundNotNullMaxBoundNull(IntType type, boolean nullable) {
    BigInteger min;
    BigInteger max;
    // [i;?+inf] [?;?]
    if (type.minBound == null && type.maxBound == null) {
      // [i;?+inf] [-inf;+inf]
      return null;
    }

    if (type.minBound != null) {
      // [i;+inf] [j;?]
      if (type.minBound.compareTo(minBound) > 0) {
        // [i;?+inf] [j;?] && i < j
        if (type.maxBound != null) {
          // [i;?+inf] [j;k]
          max = type.minBound.subtract(BigInteger.ONE);
          IntType tmp1 = new IntType(nullable, minBound, max);

          min = type.maxBound.add(BigInteger.ONE);
          IntType tmp2 = new IntType(nullable, min, null);
          return Types.union(tmp1, tmp2);
        }
        // [i;?+inf] [j;+inf]
        min = minBound;
        max = type.minBound.subtract(BigInteger.ONE);
        return new IntType(nullable, min, max);
      } else {
        // [i;?+inf] [j;?] && i >= j
        if (type.maxBound == null) {
          // [i;?+inf] [j;+inf] && i >= j
          return null;
        }
        // [i;?+inf] [j;k] && i >= j
        if (type.maxBound.compareTo(minBound) < 0) {
          // [i;?+inf] [j;k] && i >= j && i > k
          return this;
        }
        // [i;?+inf] [j;k] && i >= j && i <= k
        min = type.maxBound.add(BigInteger.ONE);
        return new IntType(nullable, min, null);
      }
    } else {
      // [i;?+inf] [-inf;j]
      if (type.maxBound.compareTo(minBound) > 0) {
        // [i;?+inf] [-inf;j] && j > i
        min = type.maxBound.add(BigInteger.ONE);
        return new IntType(nullable, min, null);
      } else {
        // [i;?+inf] [-inf;j] && j <= i
        return this;
      }
    }
  }
  
  @Override
  public Type invert() {
    if (minBound == null) {
      if (maxBound == null) {
        return null;
      }
    }
    
    Type result;
    if (minBound != null) {
      result = new IntType(isNullable(), null, minBound.subtract(BigInteger.ONE));
      if (maxBound != null) {
        result = Types.union(result, new IntType(isNullable(), maxBound.add(BigInteger.ONE), null));
      }
      return result;
    }
    
    return new IntType(isNullable(), maxBound.add(BigInteger.ONE), null);
  }
}
