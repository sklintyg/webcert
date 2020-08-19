import * as React from 'react';
import {editCertificate, editCertificateNew, ICertificateContent, showValidationError} from "../../store/certificate/certificateSlice";
import {useDispatch, useSelector} from "react-redux";
import {Radio, FormControlLabel, Typography} from '@material-ui/core';
import {makeStyles} from "@material-ui/core/styles";
import {useAppDispatch} from "../../store/store";
import {CertificateBooleanValue, CertificateDataElement, CertificateTextValue} from "../../store/domain/certificate";
import {updateCertificateDataElement} from "../../store/actions/certificates";
import {getShowValidationErrors} from "../../store/selectors/certificate";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: "#fff",
    paddingLeft: "28px",
    paddingBottom: "15px",
    // marginBottom: "15px",
    borderBottomRightRadius: "8px",
    borderBottomLeftRadius: "8px",
  },
  heading: {
    fontWeight: "bold",
  },
}));

type Props = {
  question: CertificateDataElement
};

const UeRadio: React.FC<Props> = ({question}) => {
  const booleanValue = getBooleanValue(question);
  const isShowValidationError = useSelector(getShowValidationErrors);
  const dispatch = useAppDispatch();

  const styles = useStyles();

  const handleChange: React.ChangeEventHandler<HTMLInputElement> = event => {
    const updatedValue = getUpdatedValue(question, event.currentTarget.value === "true");
    console.log('updatedValue', updatedValue);
    dispatch(updateCertificateDataElement(updatedValue));
  };

  if (!booleanValue) {
    return <div className={styles.root}>Value not supported!</div>;
  }

  return (
    <React.Fragment>
      <div className={styles.root}>
        <FormControlLabel label={booleanValue.selectedText} control={
          <Radio color="default" name={question.config.prop + "true"} value={true} onChange={e => handleChange(e)} checked={
            booleanValue.selected !== null && booleanValue.selected
          } />
        } />

        <FormControlLabel label={booleanValue.unselectedText} control={
          <Radio color="default" name={question.config.prop + "false"} value={false} onChange={e => handleChange(e)} checked={
            booleanValue.selected !== null && !booleanValue.selected
          } />
        } />
        {isShowValidationError && question.validationErrors && question.validationErrors.length > 0 && question.validationErrors.map(validationError => <Typography variant="body1" color="error">{validationError.text}</Typography>)}
      </div>
    </React.Fragment>
  );
};

function getBooleanValue(question: CertificateDataElement): CertificateBooleanValue | null {
  if (question.value.type !== "BOOLEAN") {
    return null;
  }
  return question.value as CertificateBooleanValue;
}

function getUpdatedValue(question: CertificateDataElement, selected: boolean) : CertificateDataElement {
  const updatedQuestion: CertificateDataElement = { ...question };
  updatedQuestion.value = { ...updatedQuestion.value };
  (updatedQuestion.value as CertificateBooleanValue).selected = selected;
  return updatedQuestion;
}

export default UeRadio;
