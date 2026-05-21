package http

import (
	"encoding/json"
	"errors"
	"fmt"
	"strings"

	"github.com/go-playground/validator/v10"
)

func validationMessage(err error) string {
	var syntaxError *json.SyntaxError
	if errors.As(err, &syntaxError) {
		return "invalid JSON body"
	}

	var unmarshalTypeError *json.UnmarshalTypeError
	if errors.As(err, &unmarshalTypeError) {
		if unmarshalTypeError.Field != "" {
			return fmt.Sprintf("%s has invalid type", unmarshalTypeError.Field)
		}
		return "invalid JSON body"
	}

	if strings.HasPrefix(err.Error(), "json: unknown field ") {
		field := strings.Trim(strings.TrimPrefix(err.Error(), "json: unknown field "), "\"")
		return fmt.Sprintf("%s is not allowed", field)
	}

	var validationErrors validator.ValidationErrors
	if errors.As(err, &validationErrors) {
		return formatValidationErrors(validationErrors)
	}

	return "invalid request body"
}

func formatValidationErrors(errs validator.ValidationErrors) string {
	messages := make([]string, 0, len(errs))
	for _, err := range errs {
		messages = append(messages, validationErrorMessage(err))
	}
	return strings.Join(messages, ", ")
}

func validationErrorMessage(err validator.FieldError) string {
	field := jsonFieldName(err.Field())

	switch err.Tag() {
	case "required":
		return fmt.Sprintf("%s is required", field)
	case "uuid":
		return fmt.Sprintf("%s must be a valid uuid", field)
	case "oneof":
		return fmt.Sprintf("%s must be one of: %s", field, err.Param())
	case "gt":
		return fmt.Sprintf("%s must be greater than %s", field, err.Param())
	default:
		return fmt.Sprintf("%s is invalid", field)
	}
}

func jsonFieldName(field string) string {
	if field == "" {
		return "field"
	}
	return strings.ToLower(field[:1]) + field[1:]
}
