import * as React from 'react';
import {connect, Dispatch} from 'react-redux';

import '~/sass/elements/link.scss';
import '~/sass/elements/application-panel.scss';
import '~/sass/elements/text.scss';
import '~/sass/elements/buttons.scss';
import '~/sass/elements/form-fields.scss';
import Link from '~/components/general/link';
import {i18nType} from '~/reducers/base/i18n';
import {DiscussionAreaListType} from '~/reducers/main-function/discussion/discussion-areas';
import {DiscussionType} from '~/reducers/main-function/discussion/discussion-threads';

interface DiscussionToolbarProps {
  i18n: i18nType,
  areas: DiscussionAreaListType,
  discussionThreads: DiscussionType
}

interface DiscussionToolbarState {
}

class CommunicatorToolbar extends React.Component<DiscussionToolbarProps, DiscussionToolbarState> {
  constructor(props: DiscussionToolbarProps){
    super(props);
  }
  render(){
    return <div className="application-panel__toolbar">
      <div className="container container--new-message-toolbar-container">
        <Link className="button button--new-message">
          {this.props.i18n.text.get('plugin.communicator.newMessage.label')}
        </Link>
      </div>
      <select className="form-field form-field--toolbar-selector" value={this.props.discussionThreads.areaId}>
        <option value={null}>{this.props.i18n.text.get("plugin.discussion.browseareas.all")}</option>
        {this.props.areas.map((area)=><option key={area.id} value={area.id}>
          {area.name}
        </option>)}
      </select>
      <Link className="button-pill button-pill--discussion-toolbar">
        <span className="icon icon-add"></span>
      </Link>
      <Link className="button-pill button-pill--discussion-toolbar">
        <span className="icon icon-edit"></span>
      </Link>
      <Link className="button-pill button-pill--discussion-toolbar">
        <span className="icon icon-delete"></span>
      </Link>
    </div>
  }
}

function mapStateToProps(state: any){
  return {
    i18n: state.i18n,
    areas: state.areas,
    discussionThreads: state.discussionThreads
  }
};

function mapDispatchToProps(dispatch: Dispatch<any>){
  return {}
};

export default (connect as any)(
  mapStateToProps,
  mapDispatchToProps
)(CommunicatorToolbar);