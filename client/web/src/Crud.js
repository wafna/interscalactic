import React from 'react';
import {Reactor} from './Reactor';
import * as U from "./Util";
import {Button, Icon, Input} from './Widgets';
import {api} from "./API";
export class Crud extends Reactor {
  constructor(props) {
    super(props);
    super.lenses({users: null, selected: null});
    this.updateUsers = this.updateUsers.bind(this);
  }
  updateUsers() {
    this.lens.selected.put(null);
    api.users.list()(this.lens.users.put);
  }
  componentDidMount() {
    this.updateUsers();
  }
  render() {
    const selected = this.lens.selected.take();
    return <div>
      {(this.state.users) ? (
          <div>{this.state.users.map(user => {
                let mode = !!selected && selected.id === user.id;
                return <div key={user.id}>
                  <Icon name='delete' onClick={() => {
                    this.lens.selected.put(null);
                    api.users.deleteUser(user.id)(() => {
                      this.updateUsers();
                    });
                  }}/>
                  <Icon name='pencil' onClick={() => this.lens.selected.put(user)}/>
                  {(!selected || selected.id !== user.id) ? null :
                      <Icon name='action-undo' onClick={() => this.lens.selected.put(null)}/>}
                  <UsersPanel user={user} mode={mode} updateUsers={this.updateUsers}/>
                </div>
              }
          )}</div>
      ) : null}
      <UsersPanel updateUsers={this.updateUsers}/>
    </div>
  }
}
/**
 * Lists users and allows create, update, and delete.
 */
class UsersPanel extends Reactor {
  constructor(props) {
    super(props);
    super.lenses({mode: !!props.mode, user: U.orElse(props.user, {id: 0, givenName: '', familyName: ''})});
    // Serves up names to avoid filling in the form.
    this.dummyNames = (() => {
      let index = 0;
      const names = [['Leonard', 'Susskind'], ['Richard', 'Feynman'], ['Max', 'Tegmark'], ['Hugh', 'Everett']];
      return () => {
        const name = names[index];
        index = (1 + index) % names.length;
        return name;
      }
    })();
  }
  static getDerivedStateFromProps(nextProps, prevState) {
    void prevState;
    return {user: U.orElse(nextProps.user, {id: 0, givenName: '', familyName: ''}), mode: !!nextProps.mode};
  }
  render() {
    const self = this;
    const lens = this.lens;
    let givenName = lens.user.givenName.take();
    let familyName = lens.user.familyName.take();
    let submitEnabled = !!givenName && !!familyName;
    let clearEnabled = !!givenName || !!familyName;
    let user = this.state.user;
    let mode = this.state.mode;
    if (0 < user.id && !mode)
      return <span>{' [' + user.id + '] ' + user.givenName + ' ' + user.familyName}</span>;
    else {
      return (
          <div className="well">
            <form>
              <div><label> Given Name: <Input lens={lens.user.givenName}/> </label></div>
              <div><label> Family Name: <Input lens={lens.user.familyName}/> </label></div>
              <div><Button className="btn btn-primary" disabled={!submitEnabled} onClick={() => {
                lens.mode.put(false);
                let p = null;
                if (0 < self.state.user.id) {
                  p = api.users.updateUser(self.state.user);
                } else {
                  p = api.users.createUser(self.state.user);
                }
                p(() => self.props.updateUsers());
              }}>{self.state.mode ? 'Update' : 'Create'}</Button>
                <Button className="btn btn-warning" disabled={!clearEnabled} onClick={() => {
                  lens.user.givenName.put('');
                  lens.user.familyName.put('');
                }}>Clear</Button>
                <Button className="btn btn-secondary" onClick={() => {
                  const name = this.dummyNames();
                  lens.user.givenName.put(name[0]);
                  lens.user.familyName.put(name[1]);
                }}>Make something up</Button>
              </div>
            </form>
          </div>
      );
    }
  }
}
