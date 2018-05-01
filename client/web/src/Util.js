/**
 * For conveniently defaulting values. The default is used if v is null or undefined.
 * @param v The value to test.
 * @param d The default value which defaults to null.
 * @returns {*}
 */
export const orElse = (v, d) =>
    (v !== undefined && v !== null) ? v : (undefined === d ? null : d);
/**
 * Creates a function that takes a value and asserts that it is either null or of the indicated type.
 * @param type
 * @returns {Function}
 */
const assertType = type => {
  return v => {
    if (v === undefined) {
      throw new Error('Undefined.');
    }
    if (v !== null && typeof v !== type) {
      throw new Error('Required ' + type + ' but got value ' + v + ' of type ' + (typeof v));
    }
  }
};
export const check = {
  assert: (cond, msg) => {
    if (!cond) {
      throw new Error(msg)
    }
  },
  isDefined: x => x !== undefined,
  notNull: x => (x !== null) && (x !== undefined),
  isString: assertType('string'),
  isNumber: assertType('number'),
  isBoolean: assertType('boolean'),
  isArray: a => {
    if (!Array.isArray(a)) {
      throw new Error('Required Array but got value ' + a + ' of type ' + (typeof a));
    }
  },
  isFunction: assertType('function')
};
/**
 *
 * @type {function(): function(): *}
 */
export const Const = (() => {
  let value = null;
  const f = () => value;
  f.set = v => {
    check.notNull(v);
    value = v;
  };
  return f;
});
