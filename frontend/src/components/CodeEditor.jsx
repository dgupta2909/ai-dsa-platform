import Editor from '@monaco-editor/react';

function CodeEditor({ language = 'java', value, onChange }) {
  return (
    <div className="code-editor-container">
      <div className="code-editor-header">
        <span>Code Editor</span>
        <span className="code-editor-language">
          {language.toUpperCase()}
        </span>
      </div>

      <Editor
        height="500px"
        language={language}
        value={value}
        onChange={(newValue) => onChange(newValue || '')}
        theme="vs-dark"
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: 'on',
          roundedSelection: false,
          scrollBeyondLastLine: false,
          automaticLayout: true,
          tabSize: 4,
          padding: {
            top: 15,
            bottom: 15,
          },
        }}
      />
    </div>
  );
}

export default CodeEditor;