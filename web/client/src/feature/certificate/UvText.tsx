import React from "react";
import {ICertificateContent} from "../../store/certificate/certificateSlice";
import {makeStyles} from "@material-ui/core/styles";
import {
  CertificateBooleanValue,
  CertificateDataElement,
  CertificateDataValueType,
  CertificateTextValue
} from "../../store/domain/certificate";
import {hideCertificateDataElement, showCertificateDataElement} from "../../store/actions/certificates";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: "#fff",
    paddingLeft: "28px",
    paddingBottom: "15px",
    // marginBottom: "15px",
    borderBottomRightRadius: "8px",
    borderBottomLeftRadius: "8px",
  },
  uvtext: {
    backgroundColor: "#e9eaed",
    padding: "8px 14px",
    display: "inline-block",
    borderRadius: "4px",
    marginTop: "5px",
    marginBottom: "5px",
  }
}));

type Props = {
  question: CertificateDataElement
}

const UvText: React.FC<Props> = ({question}) => {
  const styles = useStyles();

  const value = ""; //question.data[question.config.prop];

  let displayText = value && value !== "EMPTY" && question.visible ? value : "Ej angivet";

  if (displayText === "true") displayText = "Ja";
  if (displayText === "false") displayText = "Nej";

  // switch (question.value.type) {
  //   case CertificateDataValueType.BOOLEAN:
  //     const booleanValue = (update.value as CertificateBooleanValue).selected;
  //     if (booleanValue && !question.visible) {
  //       dispatch(showCertificateDataElement(questionId));
  //     } else if (!booleanValue && question.visible) {
  //       dispatch(hideCertificateDataElement(questionId));
  //     }
  //     break;
  //   case CertificateDataValueType.TEXT:
  //     const textValue = (update.value as CertificateTextValue).text;
  //     if (textValue != null && textValue.length > 0) {
  //       dispatch(showCertificateDataElement(questionId));
  //     } else if (question.visible) {
  //       dispatch(hideCertificateDataElement(questionId));
  //     }
  //     break;
  //   default:
  //     break;
  // }

  return (
    <div className={styles.root}>
      <label className={styles.uvtext}>{displayText}</label>
    </div>
  );
};

export default UvText;
