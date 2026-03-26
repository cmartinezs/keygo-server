export const REQUIRED_HOSTED_LOGIN_QUERY_KEYS = [
  'tenantSlug',
  'client_id',
  'redirect_uri',
  'scope',
  'response_type',
  'state',
  'code_challenge',
  'code_challenge_method',
] as const;

export type RequiredHostedLoginQueryKey =
  (typeof REQUIRED_HOSTED_LOGIN_QUERY_KEYS)[number];

export type CodeChallengeMethod = 'S256';
export type ResponseType = 'code';

export interface HostedLoginParams {
  tenantSlug: string;
  clientId: string;
  redirectUri: string;
  scope: string[];
  scopeRaw: string;
  responseType: ResponseType;
  state: string;
  codeChallenge: string;
  codeChallengeMethod: CodeChallengeMethod;
  clientName?: string;
  appDisplayName?: string;
  handoffVersion?: string;
}

export interface HostedLoginParamsInput {
  tenantSlug: string;
  clientId: string;
  redirectUri: string;
  scope: string[];
  responseType?: ResponseType;
  state: string;
  codeChallenge: string;
  codeChallengeMethod?: CodeChallengeMethod;
  clientName?: string;
  appDisplayName?: string;
  handoffVersion?: string;
}

export type HostedLoginValidationErrorCode =
  | 'MISSING_REQUIRED_PARAM'
  | 'INVALID_TENANT_SLUG'
  | 'INVALID_CLIENT_ID'
  | 'INVALID_REDIRECT_URI'
  | 'INVALID_SCOPE'
  | 'INVALID_RESPONSE_TYPE'
  | 'INVALID_STATE'
  | 'INVALID_CODE_CHALLENGE'
  | 'INVALID_CODE_CHALLENGE_METHOD';

export interface HostedLoginValidationError {
  param: string;
  code: HostedLoginValidationErrorCode;
  message: string;
}

export type HostedLoginParseResult =
  | { ok: true; value: HostedLoginParams }
  | { ok: false; errors: HostedLoginValidationError[] };

export class HostedLoginParamsError extends Error {
  constructor(public readonly errors: HostedLoginValidationError[]) {
    super(
      `Hosted login params inválidos: ${errors
        .map((error) => `${error.param}=${error.code}`)
        .join(', ')}`,
    );
    this.name = 'HostedLoginParamsError';
  }
}

const TENANT_SLUG_REGEX = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const CLIENT_ID_REGEX = /^[A-Za-z0-9._:-]{3,120}$/;
const PKCE_CHALLENGE_REGEX = /^[A-Za-z0-9._~-]{43,128}$/;

function toSearchParams(
  input: string | URLSearchParams,
): URLSearchParams {
  if (input instanceof URLSearchParams) {
    return input;
  }

  return new URLSearchParams(input.startsWith('?') ? input.slice(1) : input);
}

function hasHttpProtocol(value: string): boolean {
  try {
    const url = new URL(value);
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch {
    return false;
  }
}

function normalizeScope(rawScope: string): string[] {
  return [...new Set(rawScope.split(/\s+/).map((scope) => scope.trim()).filter(Boolean))];
}

function readRequired(
  searchParams: URLSearchParams,
  key: RequiredHostedLoginQueryKey,
  errors: HostedLoginValidationError[],
): string {
  const value = searchParams.get(key)?.trim() ?? '';

  if (!value) {
    errors.push({
      param: key,
      code: 'MISSING_REQUIRED_PARAM',
      message: `El parámetro '${key}' es obligatorio para hosted login.`,
    });
  }

  return value;
}

function validateTenantSlug(
  tenantSlug: string,
  errors: HostedLoginValidationError[],
): void {
  if (tenantSlug && !TENANT_SLUG_REGEX.test(tenantSlug)) {
    errors.push({
      param: 'tenantSlug',
      code: 'INVALID_TENANT_SLUG',
      message:
        "'tenantSlug' debe usar slug kebab-case (ej. acme-corp).",
    });
  }
}

function validateClientId(
  clientId: string,
  errors: HostedLoginValidationError[],
): void {
  if (clientId && !CLIENT_ID_REGEX.test(clientId)) {
    errors.push({
      param: 'client_id',
      code: 'INVALID_CLIENT_ID',
      message:
        "'client_id' contiene caracteres inválidos o no cumple longitud mínima.",
    });
  }
}

function validateRedirectUri(
  redirectUri: string,
  errors: HostedLoginValidationError[],
): void {
  if (redirectUri && !hasHttpProtocol(redirectUri)) {
    errors.push({
      param: 'redirect_uri',
      code: 'INVALID_REDIRECT_URI',
      message: "'redirect_uri' debe ser una URL absoluta http(s).",
    });
  }
}

function validateScope(
  scopeRaw: string,
  scope: string[],
  errors: HostedLoginValidationError[],
): void {
  if (scopeRaw && scope.length === 0) {
    errors.push({
      param: 'scope',
      code: 'INVALID_SCOPE',
      message: "'scope' debe contener al menos un scope separado por espacios.",
    });
  }
}

function validateResponseType(
  responseType: string,
  errors: HostedLoginValidationError[],
): void {
  if (responseType && responseType !== 'code') {
    errors.push({
      param: 'response_type',
      code: 'INVALID_RESPONSE_TYPE',
      message: "Hosted login solo soporta 'response_type=code'.",
    });
  }
}

function validateState(
  state: string,
  errors: HostedLoginValidationError[],
): void {
  if (state && state.length < 8) {
    errors.push({
      param: 'state',
      code: 'INVALID_STATE',
      message: "'state' debe tener al menos 8 caracteres.",
    });
  }
}

function validateCodeChallenge(
  codeChallenge: string,
  errors: HostedLoginValidationError[],
): void {
  if (codeChallenge && !PKCE_CHALLENGE_REGEX.test(codeChallenge)) {
    errors.push({
      param: 'code_challenge',
      code: 'INVALID_CODE_CHALLENGE',
      message:
        "'code_challenge' debe venir en formato PKCE base64url (43-128 chars).",
    });
  }
}

function validateCodeChallengeMethod(
  codeChallengeMethod: string,
  errors: HostedLoginValidationError[],
): void {
  if (codeChallengeMethod && codeChallengeMethod !== 'S256') {
    errors.push({
      param: 'code_challenge_method',
      code: 'INVALID_CODE_CHALLENGE_METHOD',
      message: "Hosted login solo acepta 'code_challenge_method=S256'.",
    });
  }
}

interface HostedLoginValidationContext {
  tenantSlug: string;
  clientId: string;
  redirectUri: string;
  scopeRaw: string;
  scope: string[];
  responseType: string;
  state: string;
  codeChallenge: string;
  codeChallengeMethod: string;
  errors: HostedLoginValidationError[];
}

const HOSTED_LOGIN_VALIDATORS: ReadonlyArray<
  (input: HostedLoginValidationContext) => void
> = [
  (input) => validateTenantSlug(input.tenantSlug, input.errors),
  (input) => validateClientId(input.clientId, input.errors),
  (input) => validateRedirectUri(input.redirectUri, input.errors),
  (input) => validateScope(input.scopeRaw, input.scope, input.errors),
  (input) => validateResponseType(input.responseType, input.errors),
  (input) => validateState(input.state, input.errors),
  (input) => validateCodeChallenge(input.codeChallenge, input.errors),
  (input) =>
    validateCodeChallengeMethod(input.codeChallengeMethod, input.errors),
];

function pushFormatErrors(input: HostedLoginValidationContext): void {
  HOSTED_LOGIN_VALIDATORS.forEach((validator) => validator(input));
}

export function parseHostedLoginParams(
  input: string | URLSearchParams,
): HostedLoginParseResult {
  const searchParams = toSearchParams(input);
  const errors: HostedLoginValidationError[] = [];

  const tenantSlug = readRequired(searchParams, 'tenantSlug', errors);
  const clientId = readRequired(searchParams, 'client_id', errors);
  const redirectUri = readRequired(searchParams, 'redirect_uri', errors);
  const scopeRaw = readRequired(searchParams, 'scope', errors);
  const responseType = readRequired(searchParams, 'response_type', errors);
  const state = readRequired(searchParams, 'state', errors);
  const codeChallenge = readRequired(searchParams, 'code_challenge', errors);
  const codeChallengeMethod = readRequired(
    searchParams,
    'code_challenge_method',
    errors,
  );

  const scope = normalizeScope(scopeRaw);

  pushFormatErrors({
    tenantSlug,
    clientId,
    redirectUri,
    scopeRaw,
    scope,
    responseType,
    state,
    codeChallenge,
    codeChallengeMethod,
    errors,
  });

  if (errors.length > 0) {
    return { ok: false, errors };
  }

  return {
    ok: true,
    value: {
      tenantSlug,
      clientId,
      redirectUri,
      scope,
      scopeRaw: scope.join(' '),
      responseType: 'code',
      state,
      codeChallenge,
      codeChallengeMethod: 'S256',
      clientName: searchParams.get('client_name')?.trim() || undefined,
      appDisplayName:
        searchParams.get('app_display_name')?.trim() || undefined,
      handoffVersion: searchParams.get('handoff_version')?.trim() || undefined,
    },
  };
}

export function assertHostedLoginParams(
  input: string | URLSearchParams,
): HostedLoginParams {
  const result = parseHostedLoginParams(input);
  if (!result.ok) {
    throw new HostedLoginParamsError(result.errors);
  }
  return result.value;
}

export function buildHostedLoginQuery(
  input: HostedLoginParamsInput,
): string {
  const searchParams = new URLSearchParams({
    tenantSlug: input.tenantSlug,
    client_id: input.clientId,
    redirect_uri: input.redirectUri,
    scope: input.scope.join(' '),
    response_type: input.responseType ?? 'code',
    state: input.state,
    code_challenge: input.codeChallenge,
    code_challenge_method: input.codeChallengeMethod ?? 'S256',
  });

  if (input.clientName) {
    searchParams.set('client_name', input.clientName);
  }

  if (input.appDisplayName) {
    searchParams.set('app_display_name', input.appDisplayName);
  }

  if (input.handoffVersion) {
    searchParams.set('handoff_version', input.handoffVersion);
  }

  return searchParams.toString();
}

export function toAuthorizeQuery(params: HostedLoginParams): string {
  const searchParams = new URLSearchParams({
    client_id: params.clientId,
    redirect_uri: params.redirectUri,
    scope: params.scopeRaw,
    response_type: params.responseType,
    state: params.state,
    code_challenge: params.codeChallenge,
    code_challenge_method: params.codeChallengeMethod,
  });

  return searchParams.toString();
}





