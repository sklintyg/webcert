import React from "react";
import {useSelector} from "react-redux";
import Category from "./Category";
import Question from "./Question";
import {styled} from "@material-ui/core/styles";
import {CertificateFooter} from "./CertificateFooter";
import CertificateValidation from "./CertificateValidation";
import {getCertificateDataElements, getIsShowSpinner, getSpinnerText} from "../../store/selectors/certificate";
import {Box, Container, CircularProgress, Backdrop} from "@material-ui/core";

const RootBox = styled(Box)({
  padding: "1px 10px 10px 10px",
  backgroundColor: "#d7d7dd",
  margin: "0 auto",
});

type Props = {};

const Certificate: React.FC<Props> = () => {
  const certificateStructure = useSelector(getCertificateDataElements);
  const showSpinner = useSelector(getIsShowSpinner);
  const spinnerText = useSelector(getSpinnerText);

  console.log("certificate");

  if (showSpinner) return (
    <Backdrop open={showSpinner}>
      <Box display="flex" flexDirection="column" alignItems="center">
        <CircularProgress />
        <h1>{spinnerText}</h1>
      </Box>
    </Backdrop>);

  return (
    <RootBox>
      {certificateStructure && certificateStructure.map((data) => {
        if (data.component === "category") {
          return <Category key={data.id} id={data.id} />
        } else {
          return <Question key={data.id} id={data.id} />
        }
      })}
      <CertificateValidation />
      <CertificateFooter />
    </RootBox>
  );
}

export default Certificate;
