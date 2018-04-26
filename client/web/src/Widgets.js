import React from 'react';
import './App.css';
export function Input(props) {
  return <input value={props.lens.take()} onChange={e => {
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