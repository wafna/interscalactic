export const CHECK = {
  // For ensuring functions are not newed.
  PURE: self => !self || (() => {
    throw new Error('forbidden: new or bind');
  })()
};
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
export const assertType = type => {
  return v => {
    if (v === undefined) throw new Error('Undefined.');
    if (v !== null && typeof v !== type)
      throw new Error('Value ' + v + ' of wrong type: required ' + type + ' but found ' + (typeof v));
  }
};
assertType.string = assertType('string');
assertType.number = assertType('number');
assertType.boolean = assertType('boolean');
assertType.array = v => {
  if (!Array.isArray(v)) throw new Error('Value ' + v + ' of wrong type: required Array but found ' + (typeof v));
};
