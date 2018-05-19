import React from 'react';
import renderer from 'react-test-renderer';
import {Icon} from './Widgets';
test('icon sets default height and width', () => {
  const icon = renderer.create(<Icon name='aperture'/>);
  let tree = icon.toJSON();
  expect(tree.props.height).toBe(24);
  expect(tree.props.width).toBe(24);
  expect(tree).toMatchSnapshot();
});
test('icon sets height and width from size as object', () => {
  const icon = renderer.create(<Icon name='aperture' size={{height: 80, width: 80}}/>);
  let tree = icon.toJSON();
  expect(tree.props.height).toBe(80);
  expect(tree.props.width).toBe(80);
});
test('icon sets height and width from size as number', () => {
  const icon = renderer.create(<Icon name='aperture' size={42}/>);
  let tree = icon.toJSON();
  expect(tree.props.height).toBe(42);
  expect(tree.props.width).toBe(42);
});
