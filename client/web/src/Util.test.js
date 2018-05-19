import {check, orElse} from './Util'
test('check', () => {
  expect(check.isNumber(42)).toBeTruthy();
});