options(repos = c(CRAN = "https://cloud.r-project.org"))

if (!require("devtools")) {
  install.packages("devtools")
}
if (!require("MCMCpack")) {
  install.packages("MCMCpack")
}
if (!require("geometry")) {
  install.packages("geometry")
}
if (!require("tidyr")) {
  install.packages("tidyr")
}
if (!require("dplyr")) {
  install.packages("dplyr")
}
if (!require("ggplot2")) {
  install.packages("ggplot2")
}
if (!require("scmamp")) {
  devtools::install_github("b0rxa/scmamp")
}
if (!require("readr")) {
  install.packages("readr")
}
if (!require("reshape2")) {
  install.packages("reshape2")
}
if (!require("tidyverse")) {
  install.packages("tidyverse")
}
if (!require("PMCMRplus")) {
  install.packages("PMCMRplus")
}

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
