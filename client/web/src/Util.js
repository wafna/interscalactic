/**
 * For conveniently defaulting values. The default is used if v is null or undefined.
 * @param v The value to test.
 * @param d The default value which defaults to null.
 * @returns {*}
 */
export const orElse = (v, d) =>
    (v !== undefined && v !== null) ? v : (undefined === d ? null : d);
/**
 * For validating values.
 */
export const check = {
  isDefined: x => x !== undefined,
  isNull: x => x !== undefined,
  exists: x => (x !== null) && (x !== undefined),
  isNumber: x => 'number' === typeof x,
  isString: x => 'string' === typeof x,
  isBoolean: x => 'boolean' === typeof x,
  isArray: x => Array.isArray(x),
  // null is of type 'object'.
  isObject: x => (x !== null) && ('object' === typeof x),
  isFunction: x => ('function' === typeof x)
};
/**
 *
 * @type {function(): function(): *}
 */
export const Const = (() => {
  let value = null;
  const f = () => value;
  f.set = v => {
    if (! check.exists(v))
      throw new Error('Value must exist.');
    value = v;
  };
  return f;
});
