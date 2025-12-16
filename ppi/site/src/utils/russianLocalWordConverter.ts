export const russianLocalWordConverter = (count: number, singleForm: string, pairForm: string, teensForm: string, basicForm: string): string => {
  const lastDigit = count % 10;
  const lastTwoDigits = count % 100;

  if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
    return teensForm;
  }

  if (lastDigit === 1) {
    return singleForm;
  }

  if (lastDigit >= 2 && lastDigit <= 4) {
    return pairForm;
  }

  return basicForm;
};