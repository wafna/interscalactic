import React from "react";
/**
 * Create a lens into a component's state.
 * @param component The component containing the state.
 * @param name The top level field in the state.
 * @param path The object path within the field of the state.
 * @returns {{take: (function(): *), put: (function(*=)), focus: (function(*=))}} A Lens.
 */
const Lens = (component, name, path) => {
  const lens = {
    take: () => path.reduce((a, p) => a[p], component.state[name]),
    put: v => {
      if (v === undefined) throw new Error('Undefined values are disallowed.');
      const s = {};
      if (0 === path.length) {
        s[name] = v;
        component.setState(s);
      } else {
        const init = path.slice(0, path.length - 1);
        const last = path[path.length - 1];
        init.reduce((p, a) => {
          console.log(p, a);
          return p[a]
        }, component.state[name])[last] = v;
        component.setState(s);
      }
      return lens;
    },
    focus: n => {
      // Make a copy of the array rather than mutating the paths of all the other lens!
      const slice = path.slice();
      slice.push(n);
      return Lens(component, name, slice);
    }
  };
  return lens;
};
/**
 * A 'mixin' for stateful React components allowing projections of components (lensing) of the state.
 * A Lens is an object that proxies a field on the state object for read (take) and update (put),
 * e.g. in Widgets each input widget take a single lens with which it initializes and passes updates.
 */
export class Reactor extends React.Component {
  constructor(props) {
    super(props);
    this.lens = {};
  }
  /**
   * Recursively makes lenses for each field in the initialState and sets the component's initial state.
   * Thus, there will be a lens at each node and each lens will contain fields for each branch in the object tree.
   * The resulting object in this.lens will be isomorphic to the initial state  except that each node will also be a
   * lens in addition to being an object for the purposes of object navigation.
   * NB this is called in lieu of setting state in derived classes.
   * NB Only to be called from constructor!
   * NB Don't set state after calling this!
   */
  lenses(initialState) {
    const updateSeg = (lens, seg) => {
      // wat: typeof null === 'object'
      if (seg !== null && typeof seg === 'object') {
        Object.keys(seg).forEach(key => {
          lens[key] = lens.focus(key);
          updateSeg(lens[key], seg[key]);
        });
      }
    };
    const self = this;
    Object.keys(initialState).forEach(key => {
      self.lens[key] = Lens(self, key, []);
      updateSeg(self.lens[key], initialState[key])
    });
    // warns about setting state directly
    // eslint-disable-next-line
    this.state = initialState;
  }
}
