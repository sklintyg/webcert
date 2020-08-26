import { styled } from "@material-ui/core/styles"
import { Box } from "@material-ui/core"
import constants from "./constants";
import colors from "./colors";


export const FlexColumnContainer = styled(Box)({
  display: "flex",
  flexDirection: "column",
  height: "100%",
});

export const PageHeaderContainer = styled(Box)({
  margin: "auto",
  width: "100%",
  height: "100%",
  maxWidth: constants.maxContentWidth,
});

export const ScrollingContainer = styled(Box)({
  overflowY: "auto",
  backgroundColor: colors.IA_COLOR_07,
  height: "100%",
  margin: "auto",
  width: "100%",
  maxWidth: constants.maxContentWidth,
});

export const WorkareaContainer = styled(Box)({
  padding: "30px",
});

export const Section = styled(Box)({
  paddingBottom: "16px",
});

export const ErrorSection = styled(Box)({
  borderTop: "1px solid #dee2e6",
});

export const ErrorWrapper = styled(Box)({
  margin: "15px 15px 0 15px",
});

export const CustomScrollingContainer = styled(Box)({
  maxWidth: "none",
});

export const PageContainer = styled(WorkareaContainer)({
  margin: "auto",
  width: "100%",
  maxWidth: constants.maxContentWidth,
  display: "flex",
  flexDirection: "column",
  minHeight: "calc(100vh - 290px)",
  paddingBottom: "60px",
  img: {
    width: "100%",
    paddingBottom: "20px",
  }
});

// export const NoWrapTd = styled.td`
//   white-space: nowrap;
// `
