import * as React from 'react';
import {useSelector} from "react-redux";
import {Box, ButtonGroup, Container, Typography} from "@material-ui/core";
import {getCertificateMetaData, getIsShowSpinner, getIsValidating, getIsValidForSigning} from "../../store/selectors/certificate";
import ErrorOutlineIcon from '@material-ui/icons/ErrorOutline';
import PrintIcon from '@material-ui/icons/Print';
import DeleteIcon from '@material-ui/icons/Delete';
import SyncAltIcon from '@material-ui/icons/SyncAlt';
import CheckIcon from '@material-ui/icons/Check';
import Divider from "@material-ui/core/Divider";
import Button from "@material-ui/core/Button";
import {CertificateStatus} from "../../store/domain/certificate";

type Props = {};
export const CertificateHeader: React.FC = props => {
  const certificateMetadata = useSelector(getCertificateMetaData);
  const isValidForSigning = useSelector(getIsValidForSigning);
  const isValidating = useSelector(getIsValidating);
  const isShowSpinner = useSelector(getIsShowSpinner);

  if (!certificateMetadata || isShowSpinner) {
    return null;
  }

  return (
    <Box boxShadow="0 2px 4px 0 rgba(0,0,0,.12)" borderBottom="1px solid #d7d7dd">
      <Container>
        <Box marginLeft="10px" marginRight="10px">
          <Box marginTop="10px" marginBottom="5px" display="flex" alignItems="center">
            {isValidForSigning ? (<Box clone color="green">
              <CheckIcon fontSize="small" />
            </Box>) : <ErrorOutlineIcon color="error" fontSize="small" />}
            <Box marginLeft="5px" marginRight="30px" flexGrow={isValidating ? 1 : 0}>
              <Typography variant="body2">
                {certificateMetadata.status === CertificateStatus.UNSIGNED ?
                  (isValidForSigning ? "Klar att signera" : "Obligatoriska uppgifter saknas") :
                  "Intyget är skickat till Arbetsförmedlingen"}
              </Typography>
            </Box>
            {!isValidating && (
              <>
              <Box clone color="green">
                <CheckIcon fontSize="small" />
              </Box>
              <Box flexGrow="1">
                <Typography variant="body2">
                  {certificateMetadata.status === CertificateStatus.UNSIGNED ? "Utkastet är sparat" : "Intyget är tillgängligt för patienten"}
                </Typography>
              </Box>
              </>)
            }
            {certificateMetadata.status === CertificateStatus.UNSIGNED && (
              <Typography variant="body2">
                Utkastet skapades 2020-08-25 14:37
              </Typography>
            )}
          </Box>
          <Divider />
          <Box display="flex">
            <Box flexGrow="1">
              <Typography variant={"h5"}>
                {certificateMetadata.certificateName}
              </Typography>
              <Typography component={"div"} variant="h6">
                <Box fontWeight="fontWeightBold">
                  Tolvan Tolvansson - 19121212-1212
                </Box>
              </Typography>
            </Box>
            <Box display="flex" alignItems="flex-end" marginBottom="5px">
              {certificateMetadata.status === CertificateStatus.UNSIGNED ?
                <ButtonGroup variant="outlined" color="secondary">
                  <Button startIcon={<PrintIcon />}>Skriv ut</Button>
                  <Button startIcon={<DeleteIcon />}>Radera</Button>
                </ButtonGroup> :
                <ButtonGroup variant="outlined" color="secondary">
                  <Button startIcon={<PrintIcon />}>Skriv ut</Button>
                  <Button startIcon={<SyncAltIcon />}>Ersätt</Button>
                  <Button startIcon={<DeleteIcon />}>Makulera</Button>
                </ButtonGroup>}
            </Box>
          </Box>
        </Box>
      </Container>
    </Box>
  );
};
