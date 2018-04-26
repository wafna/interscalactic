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
 * Encapsulates mutability at a path within an object graph.
 * @param obj The root of the object graph.
 * @returns {function(path): {get: (function(): *), set: (function(*): *)}}
 */
export const OPath = obj => {
  return function (path) {
    let prefix = path.slice(0, path.length - 1);
    let final = path[path.length - 1];
    let s = prefix.reduce((o, s) => {
      // only fills in missing values.
      if (undefined === o[s])
        o[s] = {};
      return o[s];
    }, obj);
    return {
      get: () => s[final],
      set: v => s[final] = v
    };
  };
};