import React from "react";
import {useSelector} from "react-redux";
import {getCertificateStructure, getError, isLoading, isSigning} from "../../store/certificate/certificateSlice";
import Category from "./Category";
import Question from "./Question";
import {makeStyles} from "@material-ui/core/styles";
import {CertificateFooter} from "./CertificateFooter";
import CertificateValidation from "./CertificateValidation";

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
  const certificateStructure = useSelector(getCertificateStructure);
  const loading = useSelector(isLoading);
  const signing = useSelector(isSigning);
  const error = useSelector(getError);

  const styles = useStyles();

  console.log("certificate");

  if (loading) return <h1>Laddar utkastet...</h1>;

  if (signing) return <h1>Signerar utkastet...</h1>;

  if (error) return <h1>{error}</h1>

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
