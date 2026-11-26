library(readr)
library(reshape2)
library(devtools)
library(geometry)
library(MCMCpack)
library(scmamp)
library(tidyr)
library(dplyr)
library(ggplot2)
library(tidyverse)
library(PMCMRplus)

df <- read.csv("../../results/transformed_QIs_AUC.csv")
minimize <- TRUE # The lesser the value, the better the result?

# Pivot the data so each column is an algorithm
pivot_df <- df %>%
  pivot_wider(names_from = Algorithm, values_from = BestCost) %>%
  arrange(Repetition)

pivot_df <- pivot_df[, -1]

# Function to build Credibility Interval Plots.
getCredibleIntervalsWeights <- function(posterior.samples, interval.size=0.9) {
  qmin <- (1-interval.size)/2
  qmax <- 1-qmin
  lower.bound <- apply(posterior.samples, MARGIN=2, FUN=quantile, p=qmin) 
  upper.bound <- apply(posterior.samples, MARGIN=2, FUN=quantile, p=qmax) 
  expectation <- apply(posterior.samples, MARGIN=2, FUN=mean) 
  return (data.frame(Expected=expectation, Lower_bound=lower.bound, Upper_bound=upper.bound))
}

# Run the Bayesian Plackett-Luce model.
pivot_df <- as.data.frame(pivot_df)
pl_model <-  bPlackettLuceModel(x.matrix=pivot_df, min=minimize, nsim=5000, nchains=20, parallel=TRUE)

# Build Credible Intervals.
processed.results <- getCredibleIntervalsWeights(pl_model$posterior.weights, interval.size=0.9)
pivot_df_bayesian <- data.frame(Algorithm=rownames(processed.results), processed.results)

# Size
theme(
  plot.title = element_text(size = 20, face = "bold"),
  axis.title = element_text(size = 16),
  axis.text = element_text(size = 14),
  legend.text = element_text(size = 14),
  legend.title = element_text(size = 16),
)

pivot_df_bayesian$Algorithm <- factor(
  pivot_df_bayesian$Algorithm,
  levels = rev(c(
    "MO-GVNS",
    "Anytime MO-GVNS",
    "Anytime MO-GVNS-s",
    "Anytime MO-GVNS-g",
    "MO-VND",
    "Anytime MO-VND",
    "Anytime MO-VND-s",
    "Anytime MO-VND-g"
  ))
)


# Save the plot to a PDF file
pdf("bayesian_credible_intervals_plot.pdf", width = 10, height = 8)

# Plotting code
ggplot(pivot_df_bayesian, aes(y=Expected, ymin=Lower_bound, ymax=Upper_bound, x=Algorithm)) + geom_errorbar() + geom_point(col="darkgreen", size=2) + theme_bw(base_size = 16) + coord_flip() + labs(y="Probability of winning") + labs(x = NULL)

# Close the PDF device
dev.off()