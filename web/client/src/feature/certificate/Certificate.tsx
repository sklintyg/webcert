import React from "react";
import {useSelector} from "react-redux";
import Category from "./Category";
import Question from "./Question";
import {makeStyles} from "@material-ui/core/styles";
import {CertificateFooter} from "./CertificateFooter";
import CertificateValidation from "./CertificateValidation";
import {getCertificateDataElements, getIsShowSpinner, getSpinnerText} from "../../store/selectors/certificate";

const useStyles = makeStyles((theme) => ({
  root: {
    backgroundColor: "d7d7dd",
  },
  heading: {
  },
  paper: {
    padding: "1px 10px 10px 10px",
    backgroundColor: "#d7d7dd",
  }
}));

type Props = {};

const Certificate: React.FC<Props> = () => {
  const certificateStructure = useSelector(getCertificateDataElements);
  const showSpinner = useSelector(getIsShowSpinner);
  const spinnerText = useSelector(getSpinnerText);

  const styles = useStyles();

  console.log("certificate");

  if (showSpinner) return <h1>{spinnerText}</h1>;

  return (
    <div className={styles.root}>
      <div className={styles.paper}>
        {certificateStructure && certificateStructure.map((data) => {
          if (data.component === "category") {
            return <Category key={data.id} id={data.id} />
          } else {
            return <Question key={data.id} id={data.id} />
          }
        })}
        <CertificateValidation />
        <CertificateFooter/>
      </div>
    </div>
  );
}

export default Certificate;
