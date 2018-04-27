import React from 'react';
import './App.css';
import * as W from './Widgets';
import * as U from './Util';
import {api} from './API';
import {Reactor} from './Reactor';
// All this to avoid the lint of having a var declaration for the index. :/
const dummyNames = (() => {
  let index = 0;
  const names = [['Leonard', 'Susskind'], ['Richard', 'Feynman'], ['Max', 'Tegmark'], ['Hugh', 'Everett']];
  return () => {
    const name = names[index];
    index = (1 + index) % names.length;
    return name;
  }
})();
class UserForm extends Reactor {
  constructor(props) {
    super(props);
    this.state = {mode: !!props.mode, user: U.orElse(props.user, {id: 0, givenName: '', familyName: ''})};
    this._user = super.lens('user');
    this._givenName = this._user.focus('givenName');
    this._familyName = this._user.focus('familyName');
    // if we're given a user the mode controls whether we're editing or displaying.
    this._mode = super.lens('mode');
  }
  static getDerivedStateFromProps(nextProps, prevState) {
    void prevState;
    return {user: U.orElse(nextProps.user, {id: 0, givenName: '', familyName: ''}), mode: !!nextProps.mode};
  }
  render() {
    let self = this;
    let givenName = this._givenName.take();
    let familyName = this._familyName.take();
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
              <div><label> Given Name: <W.Input lens={self._givenName}/> </label></div>
              <div><label> Family Name: <W.Input lens={self._familyName}/> </label></div>
              <div><W.Button className="btn btn-primary" disabled={!submitEnabled} onClick={() => {
                self._mode.put(false);
                let p = null;
                if (0 < self.state.user.id) {
                  p = api.users.updateUser(self.state.user);
                } else {
                  p = api.users.createUser(self.state.user);
                }
                p(() => self.props.updateUsers());
              }}>{self.state.mode ? 'Update' : 'Create'}</W.Button>
                <W.Button className="btn btn-warning" disabled={!clearEnabled} onClick={() => {
                  self._givenName.put('');
                  self._familyName.put('');
                }}>Clear</W.Button>
                <W.Button className="btn btn-secondary" onClick={() => {
                  const name = dummyNames();
                  self._givenName.put(name[0]);
                  self._familyName.put(name[1]);
                }}>Make something up</W.Button>
              </div>
            </form>
          </div>
      );
    }
  }
}
class App extends Reactor {
  constructor(props) {
    super(props);
    this.state = {users: null, selected: null};
    // Lenses prefaced with _ for notational clarity.
    this._users = super.lens('users');
    this._selected = super.lens('selected');
    this.updateUsers = this.updateUsers.bind(this);
  }
  updateUsers() {
    this._selected.put(null);
    api.users.list()(this._users.put);
  }
  componentDidMount() {
    this.updateUsers();
  }
  render() {
    const selected = this._selected.take();
    return (
        <main role="main">
          <div className="jumbotron">
            <div className="container">
              <h1 className="display-3"><W.Icon name='aperture' size={{height: 80, width: 80}}/>InterScalactic</h1>
              <p>Soup to nuts with React, Akka, Slick.</p>
            </div>
          </div>
          <div className="container">
            {(this.state.users) ? (
                <div>{this.state.users.map(user => {
                      let mode = !!selected && selected.id === user.id;
                      return <div key={user.id}>
                        <W.Icon name='delete' onClick={() => {
                          this._selected.put(null);
                          api.users.deleteUser(user.id)(() => {
                            this.updateUsers();
                          });
                        }}/>
                        <W.Icon name='pencil' onClick={() => this._selected.put(user)}/>
                        {(!!selected && selected.id === user.id) ?
                            <W.Icon name='action-undo' onClick={() => this._selected.put(null)}/> : null}
                        <UserForm user={user} mode={mode} updateUsers={this.updateUsers}/>
                      </div>
                    }
                )}</div>
            ) : null}
            <UserForm updateUsers={this.updateUsers}/>
          </div>
        </main>
    );
  }
}
export default App;
