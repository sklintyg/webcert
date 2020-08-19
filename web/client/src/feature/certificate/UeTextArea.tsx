import * as React from 'react';
import {editCertificateNew} from "../../store/certificate/certificateSlice";
import {useRef, useState} from "react";
import {useDispatch, useSelector} from "react-redux";
import _ from "lodash";
import {TextareaAutosize, TextField, Typography} from "@material-ui/core";
import {makeStyles} from "@material-ui/core/styles";
import {CertificateDataElement, CertificateTextValue} from "../../store/domain/certificate";
import {getShowValidationErrors} from "../../store/selectors/certificate";
import {updateCertificateDataElement} from "../../store/actions/certificates";

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
  question: CertificateDataElement
};

const UeTextArea: React.FC<Props> = ({question}) => {
  const textValue = getTextValue(question);
  const isShowValidationError = useSelector(getShowValidationErrors);
  const [text, setText] = useState(textValue != null ? textValue.text : "");
  const dispatch = useDispatch();

  const styles = useStyles();

  const dispatchEditDraft = useRef(
    _.debounce((value: string) => {
      const updatedValue = getUpdatedValue(question, value);
      console.log('updatedValue', updatedValue);
      dispatch(updateCertificateDataElement(updatedValue));
    }, 1000)
  ).current;

  if (!textValue) {
    return <div className={styles.root}>Value not supported!</div>;
  }

  const handleChange: React.ChangeEventHandler<HTMLTextAreaElement> = event => {
    setText(event.currentTarget.value);

    dispatchEditDraft(event.currentTarget.value);
  };

  return (
    <div className={styles.root}>
      <TextareaAutosize className={styles.textarea} rows={4} name={question.config.prop} value={text} onChange={e => handleChange(e)} />
      {isShowValidationError && question.validationErrors && question.validationErrors.length > 0 && question.validationErrors.map(validationError => <Typography variant="body1" color="error">{validationError.text}</Typography>)}
    </div>
  );
};

function getTextValue(question: CertificateDataElement): CertificateTextValue | null {
  if (question.value.type !== "TEXT") {
    return null;
  }
  return question.value as CertificateTextValue;
}

function getUpdatedValue(question: CertificateDataElement, text: string) : CertificateDataElement {
  const updatedQuestion: CertificateDataElement = { ...question };
  updatedQuestion.value = { ...updatedQuestion.value };
  (updatedQuestion.value as CertificateTextValue).text = text;
  return updatedQuestion;
}

// {isShowValidationError && question.validationError.length > 0 && question.validationError.map(validationError => <Typography variant="body1" color="error">{validationError.text}</Typography>)}
export default UeTextArea;
