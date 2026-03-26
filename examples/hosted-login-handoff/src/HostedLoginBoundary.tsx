import React, { createContext, useContext } from 'react';
import {
  parseHostedLoginParams,
  type HostedLoginParams,
  type HostedLoginValidationError,
} from './hostedLoginParams';

const HostedLoginParamsContext = createContext<HostedLoginParams | null>(null);

export interface HostedLoginBoundaryProps {
  search?: string | URLSearchParams;
  children: React.ReactNode;
  invalidFallback?: React.ReactNode;
  renderInvalid?: (errors: HostedLoginValidationError[]) => React.ReactNode;
  onInvalid?: (errors: HostedLoginValidationError[]) => void;
}

function resolveSearch(search?: string | URLSearchParams): string | URLSearchParams {
  if (search) {
    return search;
  }

  if (typeof globalThis.window !== 'undefined') {
    return globalThis.window.location.search;
  }

  return '';
}

export function HostedLoginBoundary({
  search,
  children,
  invalidFallback,
  renderInvalid,
  onInvalid,
}: Readonly<HostedLoginBoundaryProps>) {
  const result = parseHostedLoginParams(resolveSearch(search));

  if (!result.ok) {
    onInvalid?.(result.errors);

    if (renderInvalid) {
      return <>{renderInvalid(result.errors)}</>;
    }

    return (
      <>
        {invalidFallback ?? (
          <div data-hosted-login-error="true">
            <p>No se pudo inicializar el hosted login.</p>
            <ul>
              {result.errors.map((error) => (
                <li key={`${error.param}:${error.code}`}>
                  {error.param}: {error.message}
                </li>
              ))}
            </ul>
          </div>
        )}
      </>
    );
  }

  return (
    <HostedLoginParamsContext.Provider value={result.value}>
      {children}
    </HostedLoginParamsContext.Provider>
  );
}

export function useHostedLoginParams(): HostedLoginParams {
  const context = useContext(HostedLoginParamsContext);

  if (!context) {
    throw new Error(
      'useHostedLoginParams debe usarse dentro de HostedLoginBoundary.',
    );
  }

  return context;
}


