import React from 'react';
import { useTheme } from '../theme/ThemeContext';
import { STRINGS, StringSet, Lang } from './strings';

export function useI18n(): { T: StringSet; lang: Lang } {
  const { tweaks } = useTheme();
  return { T: STRINGS[tweaks.lang], lang: tweaks.lang };
}
