import * as React from 'react';
import {editCertificate, editCertificateNew, ICertificateContent, showValidationError} from "../../store/certificate/certificateSlice";
import {useDispatch, useSelector} from "react-redux";
import {useCallback} from "react";
import {Radio, FormControlLabel, Typography} from '@material-ui/core';
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
  heading: {
    fontWeight: "bold",
  },
}));

type Props = {
  question: ICertificateContent
};

const UeRadio: React.FC<Props> = ({question}) => {
  const isShowValidationError = useSelector(showValidationError);
  const dispatch = useDispatch();

  const styles = useStyles();

  const dispatcher = useCallback((action) => dispatch(action), [dispatch]);

  const handleChange: React.ChangeEventHandler<HTMLInputElement> = event => {
    const data = {
      type: question.data.type,
      [question.config.prop]: event.currentTarget.value
    }
    dispatcher(editCertificateNew(question.id, data));
  };

  return (
    <React.Fragment>
      <div className={styles.root}>
        <FormControlLabel label="Ja" control={
          <Radio color="default" name={question.config.prop + "true"} value="true" onChange={e => handleChange(e)} checked={
            question.data[question.config.prop] !== "EMPTY" &&
            question.data[question.config.prop] === "true"} />
        } />

        <FormControlLabel label="Nej" control={
          <Radio color="default" name={question.config.prop + "false"} value="false" onChange={e => handleChange(e)} checked={
            question.data[question.config.prop] !== "EMPTY" &&
            question.data[question.config.prop] === "false"
          } />
        } />
        {isShowValidationError && question.validationError.length > 0 && question.validationError.map(validationError => <Typography variant="body1" color="error">{validationError.text}</Typography>)}
      </div>
    </React.Fragment>
  );
};

export default UeRadio;
