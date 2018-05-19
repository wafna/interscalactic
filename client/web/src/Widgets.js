import React from 'react';
import {orElse, check} from './Util';
export const icons = {
  CircleX: <Icon name='circle-x'/>
};
export function Icon(props) {
  let size = orElse(props.size, {height: 24, width: 24});
  // Could check this in PropType but we also need to transform.
  const extent = (() => {
    if (check.isNumber(props.size)) {
      return {height: size, width: size};
    } else if (check.isObject(size)) {
      return size;
    } else {
      throw new Error('Number or {height, width} required.');
    }
  })();
  return <img className={orElse(props.className)}
              src={'/open-iconic/svg/' + props.name + '.svg'}
              alt={orElse(props.alt, props.name)}
              height={extent.height} width={extent.width}
              onClick={e => {
                e.preventDefault();
                props.onClick && props.onClick()
              }}/>
}
export function Input(props) {
  return <input type="text" value={props.lens.take()} onChange={e => {
    props.lens.put(e.target.value);
    e.preventDefault();
  }}/>;
}
export function Button(props) {
  return <button disabled={props.disabled} onClick={e => {
    props.onClick();
    e.preventDefault();
  }}>{props.children}</button>
}
