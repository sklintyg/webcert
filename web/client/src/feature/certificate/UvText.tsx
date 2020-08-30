import * as React from 'react';
import { makeStyles } from "@material-ui/core/styles";
import {
  CertificateBooleanValue,
  CertificateDataElement,
  CertificateDataValueType,
  CertificateTextValue
} from "../../store/domain/certificate";
import grey from '@material-ui/core/colors/grey';

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
    background: grey[300],
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

const UvText: React.FC<Props> = ({ question }) => {
  const styles = useStyles();

  let displayText = "Ej angivet";

  switch (question.value.type) {
    case CertificateDataValueType.BOOLEAN:
      const booleanValue = question.value as CertificateBooleanValue;
      if (booleanValue.selected !== null && question.visible) {
        displayText = booleanValue.selected ? booleanValue.selectedText : booleanValue.unselectedText;
      }
      break;
    case CertificateDataValueType.TEXT:
      const textValue = question.value as CertificateTextValue;
      if (textValue.text != null && textValue.text.length > 0) {
        displayText = textValue.text;
      }
      break;
    default:
      break;
  }

  return (
    <div className={styles.root}>
      <label className={styles.uvtext}>{displayText}</label>
    </div>
  );
};

export default UvText;
