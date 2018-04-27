import React from 'react';
import './App.css';
import * as U from './Util';
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
export function Icon(props) {
  let size = U.orElse(props.size, {height: 24, width: 24});
  return <img src={'/open-iconic/svg/' + props.name + '.svg'} alt={U.orElse(props.alt, props.name)}
              height={size.height} width={size.width}
              onClick={e => {
                e.preventDefault();
                props.onClick && props.onClick()
              }}/>
}
export const icons = {
  CircleX: <Icon name='circle-x'/>
};