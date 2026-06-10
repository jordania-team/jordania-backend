#!/usr/bin/env bash

set -euo pipefail

region="${AWS_REGION:-us-east-1}"
repository="${ECR_REPOSITORY:-pocapi}"
limit_bytes="${ECR_STORAGE_LIMIT_BYTES:-500000000}"

listed_bytes="$(
  aws ecr describe-images \
    --region "$region" \
    --repository-name "$repository" \
    --query 'sum(imageDetails[].imageSizeInBytes)' \
    --output text
)"

if [[ "$listed_bytes" == "None" ]]; then
  listed_bytes=0
fi

printf 'ECR conservative storage: %s / %s bytes\n' "$listed_bytes" "$limit_bytes"

if (( listed_bytes > limit_bytes )); then
  printf 'ERROR: conservative ECR storage limit exceeded.\n' >&2
  exit 1
fi
