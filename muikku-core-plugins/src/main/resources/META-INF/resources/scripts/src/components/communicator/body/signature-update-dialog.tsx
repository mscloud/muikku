import * as React from 'react';
import JumboDialog from '~/components/general/jumbo-dialog';
import Link from '~/components/general/link';
import CKEditor from '~/components/general/ckeditor';
import {connect, Dispatch} from 'react-redux';
import {AnyActionType} from '~/actions';
import {bindActionCreators} from 'redux';
import communicatorMessagesActions from '~/actions/main-function/communicator/communicator-messages';
import {CommunicatorSignatureType} from '~/reducers/index.d';

const KEYCODES = {
  ENTER: 13
}

const CKEDITOR_CONFIG = {
  toolbar: [
    { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', 'RemoveFormat' ] },
    { name: 'links', items: [ 'Link' ] },
    { name: 'insert', items: [ 'Image', 'Smiley', 'SpecialChar' ] },
    { name: 'colors', items: [ 'TextColor', 'BGColor' ] },
    { name: 'styles', items: [ 'Format' ] },
    { name: 'paragraph', items: [ 'NumberedList', 'BulletedList', 'Outdent', 'Indent', 'Blockquote', 'JustifyLeft', 'JustifyCenter', 'JustifyRight'] },
    { name: 'tools', items: [ 'Maximize' ] }
  ]
}

const CKEDITOR_PLUGINS = {};

interface CommunicatorSignatureUpdateDialogProps {
  children: React.ReactElement<any>,
  isOpen: boolean,
  onClose: ()=>any,
  signature: CommunicatorSignatureType
}

interface CommunicatorSignatureUpdateDialogState {
  signature: string
}

class CommunicatorSignatureUpdateDialog extends React.Component<CommunicatorSignatureUpdateDialogProps, CommunicatorSignatureUpdateDialogState> {
  constructor(props: CommunicatorSignatureUpdateDialogProps){
    super(props);
    
    this.onCKEditorChange = this.onCKEditorChange.bind(this);
    this.handleKeydown = this.handleKeydown.bind(this);
    this.resetState = this.resetState.bind(this);
    this.update = this.update.bind(this);
    
    this.state = {
      signature: props.signature ? props.signature.signature : ""
    }
  }
  handleKeydown(code: number, closeDialog: ()=>any){
    if (code === KEYCODES.ENTER){
      this.update(closeDialog);
    }
  }
  onCKEditorChange(signature: string){
    this.setState({signature});
  }
  resetState(){
    this.setState({
      signature: this.props.signature ? this.props.signature.signature : ""
    });
  }
  update(closeDialog: ()=>any){
    this.props.updateSignature(this.state.signature.trim() || null);
    closeDialog();
  }
  render(){
    let footer = (closeDialog)=>{
      return <div className="embbed embbed-full">
        <Link className="communicator button button-large button-warn commmunicator-button-standard-cancel" onClick={closeDialog}>
         {this.props.i18n.text.get('plugin.communicator.confirmSignatureRemovalDialog.cancelButton')}
        </Link>
        <Link className="communicator button button-large communicator-button-standard-ok" onClick={this.update.bind(this, closeDialog)}>
          {this.props.i18n.text.get('plugin.communicator.settings.signatures.create')}
        </Link>
      </div>
    }
    let content = (closeDialog)=>{
      return <CKEditor width="100%" height="grow" configuration={CKEDITOR_CONFIG} extraPlugins={CKEDITOR_PLUGINS}
      onChange={this.onCKEditorChange} autofocus>{this.state.signature}</CKEditor>
    }
    return <JumboDialog onClose={this.props.onClose} isOpen={this.props.isOpen} onKeyStroke={this.handleKeydown} onOpen={this.resetState} classNameExtension="communicator" 
     title={this.props.i18n.text.get("plugin.communicator.settings.signatures")}
     content={content} footer={footer}>{this.props.children}</JumboDialog>
  }
}

function mapStateToProps(state: any){
  return {
    signature: state.communicatorMessages.signature,
    i18n: state.i18n
  }
};

function mapDispatchToProps(dispatch: Dispatch<AnyActionType>){
  return bindActionCreators(communicatorMessagesActions, dispatch);
};

export default (connect as any)(
  mapStateToProps,
  mapDispatchToProps
)(CommunicatorSignatureUpdateDialog);