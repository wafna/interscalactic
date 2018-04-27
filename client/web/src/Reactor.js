import React from "react";
import {assertType} from './Util';
/**
 * Create a lens into a component's state.
 * @param component The component containing the state.
 * @param name The top level field in the state.
 * @param path The object path within the state (below the first field).
 * @returns {{take: (function(): *), put: (function(*=)), focus: (function(*=))}} A Lens.
 */
const Lens = (component, name, path) => {
  // Needn't check the other params as they are internally controlled.
  assertType.string(name);
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
      // Make a copy of the array rather than mutating the paths of all the other lenses!
      const slice = path.slice();
      slice.push(n);
      return Lens(component, name, slice);
    }
  };
  return lens;
};
/**
 * A 'mixin' for stateful React components allowing projections of components of the state.
 */
export class Reactor extends React.Component {
  // Runtime lint flags this as a useless constructor but IDEA lint flags all derived classes' constructors if this
  // isn't here.  Sigh.
  constructor(props) {
    super(props);
  }
  /**
   * Creates an object that proxies a path on the state object for read (take) and update (put).
   * E.g. inside the component's constructor put the following...
   * <code>
   *   this.state = {user: {id: 0, name: ''};
   *   this._user = super.lens('user');
   *   this._name = this._user.focus('name');
   * </code>
   * ... and now you can pass this._name to an Input widget and it will initialize itself from the _name lens as well as
   * push all its changes into it.
   * @param name Names the top level field within state that this represents.
   * @return {*} a Lens.
   */
  lens(name) {
    return Lens(this, name, []);
  }
}
export default Reactor;
