import React from "react";
import {ICertificateContent} from "../../store/certificate/certificateSlice";
import {makeStyles} from "@material-ui/core/styles";

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
  question: ICertificateContent
}

const UvText: React.FC<Props> = ({question}) => {
  const styles = useStyles();

  const value = question.data[question.config.prop];

  let displayText = value && value !== "EMPTY" && question.visible ? value : "Ej angivet";

  if (displayText === "true") displayText = "Ja";
  if (displayText === "false") displayText = "Nej";

  return (
    <div className={styles.root}>
      <label className={styles.uvtext}>{displayText}</label>
    </div>
  );
};

export default UvText;
