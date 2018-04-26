import React from "react";
const assertFunction = (f, msg) => {
  if (typeof f !== 'function')
    throw new Error('Function required: ' + msg);
};
/**
 * Generically produces a new Lens for a notifier.
 */
const _after = (lens, notify) => {
  assertFunction(notify, '');
  const newLens = {
    take: () => lens.take(),
    put: v => {
      lens.put(v);
      notify(v);
      return newLens;
    },
    before: f => _before(newLens, f),
    after: f => _after(newLens, f)
  };
  return newLens;
};
/**
 * Generically produces a new Lens for a mutator.
 */
const _before = (lens, mutate) => {
  assertFunction(mutate);
  const newLens = {
    take: () => lens.take(),
    put: v => {
      lens.put(mutate(v));
      return newLens;
    },
    before: mutate => _before(newLens, mutate),
    after: notify => _after(newLens, notify)
  };
  return newLens;
};
/**
 * A 'mixin' for stateful React components allowing projections of components of the state.
 * These projections must exist in the state or NPEs will result.
 * When a value is put, a call to component.setState is made with the value placed at the path.
 * In the component's constructor add code like this.
 * <code>
 *   this.state = {'stuff', 'moreStuff', 'value managed by the lens, below'};
 *   this.myLens = super.lens(['stuff', 'moreStuff']);
 * </code>
 */
export class Reactor extends React.Component {
  /**
   * Creates an object that proxies a path on the state object for read (take) and update (put).
   * @param path may be an array or an argument list of strings.
   * @return {*} a Lens.
   */
  lens(path) {
    const steps = Array.isArray(path) ? path : Array.from(arguments);
    const self = this;
    const rootLens = {
      /**
       * Get the component's state  at the specified path.
       */
      take: () =>
          steps.reduce((o, s) => o[s], self.state),
      /**
       * Set the component's state at the specified path.
       */
      put: v => {
        if (v === undefined)
          throw new Error('Undefined values are forbidden.');
        // create an object with the specified path to v and set the component's state with it.
        let prefix = steps.slice(0, steps.length - 1);
        let final = steps[steps.length - 1];
        let s = prefix.reduce((o, s) => {
          o[s] = {};
          return o[s];
        }, {});
        s[final] = v;
        self.setState(s);
        return rootLens;
      },
      /**
       * Create a new Lens that executes `notify` with the new value after the value is set.
       * Useful for adding concurrent state changes.
       */
      after: notify => _after(rootLens, notify),
      /**
       * Creates a new Lens that maps new values through f before setting.
       * Useful to coerce data, like ids from select options.
       */
      before: mutate => _before(rootLens, mutate),
    };
    return rootLens;
  }
}