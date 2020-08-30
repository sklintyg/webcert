import * as React from 'react';
import { useSelector } from "react-redux";
import UeRadio from "./UeRadio";
import UeTextArea from "./UeTextArea";
import { Accordion, AccordionDetails, AccordionSummary, Typography, Paper } from "@material-ui/core";
import ExpandMoreIcon from "@material-ui/icons/ExpandMore";
import { makeStyles } from "@material-ui/core/styles";
import UvText from "./UvText";
import { CertificateDataConfig, CertificateDataElement } from "../../store/domain/certificate";
import { getQuestion } from "../../store/selectors/certificate";
import grey from '@material-ui/core/colors/grey';

const useStyles = makeStyles((theme) => ({
  root: {
    padding: "0px 28px",
  },
  accordion: {
    boxShadow: 'none',
    padding: '0px 0px',
    marginTop: "0px",
  },
  accordionSummary: {
    padding: '0px 0px',
    margin: '0px 0px',
  },
  accordionDetails: {
    background: grey[300],
    marginBottom: "1rem"
  },
  heading: {
    fontWeight: "bold",
  },
  details: {
    padding: "15px 15px 0",
    color: "black"
  },
  mandatoryIcon: {
    marginLeft: "-16px",
    marginRight: "2px",
    marginTop: "2px",
    color: "#da4453",
  },
  arrowup: {
    width: '0',
    height: '0',
    content: " ",
    left: '35px',
    // position: 'absolute',
    marginLeft: '35px',
    borderWidth: '10px',
    borderHeight: '10px',
    borderLeft: '10px solid transparent',
    borderRight: '10px solid transparent',
    borderBottom: '10px solid',
    borderBottomColor: '#e9eaed',
  },
}));

type Props = {
  id: string
};

const Question: React.FC<Props> = ({ id }) => {
  const question = useSelector(getQuestion(id));

  const styles = useStyles();

  console.log("question", id);

  if (!question || (!question.visible && !question.readOnly)) return null;

  return (
    <React.Fragment>
      <Paper>
        <div className={styles.root}>
          {getQuestionComponent(question.config, question.mandatory, question.readOnly)}
        </div>
        {question.readOnly ? getUnifiedViewComponent(question) : getUnifiedEditComponent(question)}
      </Paper>
    </React.Fragment>
  );

  function getQuestionComponent(config: CertificateDataConfig, mandatory: boolean, readOnly: boolean) {
    if (!readOnly && config.description) {
      return (
        <Accordion className={styles.accordion}>
          <AccordionSummary
            className={styles.accordionSummary}
            expandIcon={<ExpandMoreIcon />}
            aria-controls="panel1a-content"
            id="panel1a-header"
          >
            {!readOnly && mandatory && <Typography className={styles.mandatoryIcon} variant="h5">*</Typography>} <Typography variant="subtitle1">{question.config.text}</Typography>
          </AccordionSummary>
          <div className={styles.arrowup}></div>
          <AccordionDetails className={styles.accordionDetails} >
            <Typography className={styles.details}>
              {question.config.description}
            </Typography>
          </AccordionDetails>
        </Accordion>
      );
    }
    return <Typography variant="subtitle1">{question.config.text}</Typography>;
  }

  function getUnifiedEditComponent(question: CertificateDataElement) {
    if (question.config.component === "ue-radio") return <UeRadio key={question.id} question={question} />;
    if (question.config.component === "ue-textarea")
      return <UeTextArea key={question.id} question={question} />;
    return <div>Cannot find a component for: {question.config.component}</div>;
  };

  function getUnifiedViewComponent(question: CertificateDataElement) {
    return <UvText question={question} />;
  };
};

export default Question;
