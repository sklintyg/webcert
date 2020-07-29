import * as React from 'react';
import {editCertificateNew, ICertificateContent, showValidationError} from "../../store/certificate/certificateSlice";
import {useCallback, useRef, useState, Fragment} from "react";
import {useDispatch, useSelector} from "react-redux";
import _ from "lodash";
import {TextareaAutosize, TextField, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: "#fff",
    paddingLeft: "28px",
    paddingBottom: "15px",
    marginBottom: "15px",
    borderBottomRightRadius: "8px",
    borderBottomLeftRadius: "8px",
  },
  textarea: {
    width: "-webkit-fill-available",
    marginRight: "20px",
  },
  heading: {
    fontWeight: "bold",
  },
}));

type Props = {
  question: ICertificateContent
};

const UeTextArea: React.FC<Props> = ({question}) => {
  const isShowValidationError = useSelector(showValidationError);
  const [text, setText] = useState(question.data[question.config.prop] || "");
  const dispatch = useDispatch();

  const styles = useStyles();

  const dispatcher = useCallback((action) => dispatch(action), [dispatch]);

  const dispatchEditDraft = useRef(
    _.debounce((value: string) => {
      const data = {
        type: question.data.type,
        [question.config.prop]: value
      }
      dispatcher(editCertificateNew(question.id, data));
    }, 1000)
  ).current;

  const handleChange: React.ChangeEventHandler<HTMLTextAreaElement> = event => {
    setText(event.currentTarget.value);

    dispatchEditDraft(event.currentTarget.value);
  };

  return (
    <div className={styles.root}>
        <TextareaAutosize className={styles.textarea} rows={4} name={question.config.prop} value={text} onChange={e => handleChange(e)} />
        {isShowValidationError && question.validationError.length > 0 && question.validationError.map(validationError => <Typography variant="body1" color="error">{validationError.text}</Typography>)}
    </div>
  );
};

export default UeTextArea;
